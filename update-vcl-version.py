import os
import re
import subprocess
from pathlib import Path

# 使用方式
# 复制文件到多个项目的文件夹 update-vcl-version.py
# 命令行：python .\update-vcl-version.py

def should_skip_directory(dirname):
    """判断是否应该跳过该目录"""
    return dirname == 'build'

def find_git_repositories(start_path):
    """找到当前目录下的所有git仓库根目录"""
    git_repos = set()

    for root, dirs, files in os.walk(start_path):
        # 过滤掉不需要遍历的目录
        dirs[:] = [d for d in dirs if not should_skip_directory(d)]

        if '.git' in dirs:
            try:
                # 验证这确实是一个git仓库的根目录
                git_root = subprocess.check_output(
                    ['git', 'rev-parse', '--show-toplevel'],
                    cwd=root,
                    stderr=subprocess.DEVNULL
                ).decode('utf-8').strip()
                git_repos.add(git_root)
                # 找到.git目录后，不需要继续遍历其子目录
                dirs.remove('.git')
            except subprocess.CalledProcessError:
                continue

    return sorted(git_repos)  # 排序以保证处理顺序一致

def find_settings_files(git_root):
    """在git仓库中查找所有settings.gradle文件"""
    settings_files = []

    for root, dirs, files in os.walk(git_root):
        # 过滤掉不需要遍历的目录
        dirs[:] = [d for d in dirs if not should_skip_directory(d)]
            
        if 'settings.gradle' in files:
            settings_files.append(os.path.join(root, 'settings.gradle'))
        if 'settings.gradle.kts' in files:
            settings_files.append(os.path.join(root, 'settings.gradle.kts'))
    
    return settings_files

def update_version_in_file(file_path, new_version):
    """更新文件中的版本号，返回是否进行了更新"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    pattern = r'id\("io\.github\.5hmlA\.vcl"\)\s+version\s+"([^"]+)"'
    match = re.search(pattern, content)
    
    if not match:
        return False, None
    
    current_version = match.group(1)
    if current_version == new_version:
        print(f"已经是最新版本 {new_version} in {file_path}")
        return False, current_version
    
    new_content = re.sub(pattern, f'id("io.github.5hmlA.vcl") version "{new_version}"', content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    
    print(f"更新文件 {file_path} 版本号: {current_version} -> {new_version}")
    return True, current_version

def update_vcl_version(new_version, should_push=False):
    # 1. 找到所有git仓库
    git_repos = find_git_repositories('.')
    if not git_repos:
        print("当前目录下未找到git仓库")
        return
    
    print(f"找到 {len(git_repos)} 个git仓库")
    
    # 2. 处理每个git仓库
    for git_root in git_repos:
        print(f"\n处理git仓库: {git_root}")
        
        # 找到仓库中的所有settings.gradle文件
        settings_files = find_settings_files(git_root)
        if not settings_files:
            print(f"在仓库 {git_root} 中未找到settings.gradle文件")
            continue
        
        # 记录是否有文件被更新
        has_updates = False
        updated_files = []
        
        # 更新所有settings文件中的版本号
        for settings_file in settings_files:
            updated, old_version = update_version_in_file(settings_file, new_version)
            if updated:
                has_updates = True
                updated_files.append(settings_file)
        
        # 如果有更新，执行git操作
        if has_updates:
            try:
                # 将所有更新的文件添加到git
                for file_path in updated_files:
                    rel_path = os.path.relpath(file_path, git_root)
                    subprocess.run(['git', 'add', rel_path], 
                                cwd=git_root, 
                                check=True)
                
                # 创建一个commit
                commit_message = f"build: 更新 vcl 插件版本到 {new_version}"
                subprocess.run(['git', 'commit', '-m', commit_message], 
                            cwd=git_root, 
                            check=True)
                print(f"在 {git_root} 创建了commit")
                
                # 如果需要push
                if should_push:
                    print(f"Push changes in {git_root}...")
                    subprocess.run(['git', 'push'], 
                                cwd=git_root, 
                                check=True)
                    print(f"Push完成: {git_root}")
            
            except subprocess.CalledProcessError as e:
                print(f"Git操作失败 in {git_root}: {str(e)}")

if __name__ == "__main__":
    # 获取用户输入的新版本号
    new_version = input("请输入新的vcl插件版本号: ")
    
    # 询问是否需要push
    push_input = input("是否需要git push? (y/N): ").lower()
    should_push = push_input == 'y' or push_input == 'yes'
    
    update_vcl_version(new_version, should_push)
