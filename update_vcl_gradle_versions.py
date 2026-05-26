import os
import re
import subprocess
from pathlib import Path

def get_git_remote_url(dir_path):
    """
    判断一个目录是否是合法的 git 仓库，并返回其 remote origin url。
    如果不是 git 仓库或没有配置远程地址，返回 None。
    """
    if not os.path.isdir(os.path.join(dir_path, '.git')):
        return None
    try:
        # 通过 git 命令获取远程仓库的 url 
        url = subprocess.check_output(
            ['git', 'config', '--get', 'remote.origin.url'],
            cwd=dir_path,
            stderr=subprocess.DEVNULL
        ).decode('utf-8').strip()
        return url if url else None
    except (subprocess.CalledProcessError, FileNotFoundError):
        return None

def find_file_in_dir(start_path, target_filename):
    """在指定目录下向下查找特定的文件（如 gradle-wrapper.properties 或 settings.gradle）"""
    for root, dirs, files in os.walk(start_path):
        # 略过 build 编译目录，提升搜索性能
        if 'build' in dirs:
            dirs.remove('build')
        if target_filename in files:
            return os.path.join(root, target_filename)
    return None

def update_gradle_version(file_path, new_version):
    """更新 gradle-wrapper.properties 中的 Gradle 版本"""
    if not os.path.exists(file_path):
        return False, None

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 匹配 distributionUrl 中的 gradle 版本号
    pattern = r'distributionUrl=.*?gradle-(.*?)-(all|bin)\.zip'
    match = re.search(pattern, content)

    if not match:
        return False, None

    current_version = match.group(1)
    
    # 版本一致则不修改
    if current_version == new_version:
        print(f"  ℹ️  Gradle 已经是目标版本 {new_version}，跳过修改。")
        return False, current_version

    # 替换为腾讯云镜像（保留原有的 all 或 bin 类型）
    zip_type = match.group(2)
    new_url = f"distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-{new_version}-{zip_type}.zip"
    new_content = re.sub(r'distributionUrl=.*', new_url, content)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)

    print(f"  📝 已更新 Gradle: {current_version} ➡️ {new_version}")
    return True, current_version

def update_vcl_version(file_path, new_version):
    """更新 settings.gradle(.kts) 中的 vcl 插件版本"""
    if not os.path.exists(file_path):
        return False, None

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    pattern = r'id\("io\.github\.5hmlA\.vcl"\)\s+version\s+"([^"]+)"'
    match = re.search(pattern, content)

    if not match:
        return False, None

    current_version = match.group(1)
    if current_version == new_version:
        print(f"  ℹ️  VCL 插件已经是目标版本 {new_version}，跳过修改。")
        return False, current_version

    new_content = re.sub(pattern, f'id("io.github.5hmlA.vcl") version "{new_version}"', content)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)

    print(f"  📝 已更新 VCL 插件: {current_version} ➡️ {new_version}")
    return True, current_version

def run_git_operations(repo_path, updated_files, vcl_ver, gradle_ver, should_push):
    """执行 Git 的 add, commit 和 push 操作"""
    try:
        # 1. git add
        for file_path in updated_files:
            rel_path = os.path.relpath(file_path, repo_path)
            subprocess.run(['git', 'add', rel_path], cwd=repo_path, check=True)
        
        # 2. 拼装带 Emoji 的优雅 Commit Message
        commit_parts = []
        if gradle_ver:
            commit_parts.append(f"Gradle to {gradle_ver}")
        if vcl_ver:
            commit_parts.append(f"VCL to {vcl_ver}")
        
        commit_msg = f"build(deps): 🚀 update " + " and ".join(commit_parts)
        
        # 3. git commit
        subprocess.run(['git', 'commit', '-m', commit_msg], cwd=repo_path, check=True)
        print(f"  ✅ Git 提交成功! Commit: \"{commit_msg}\"")

        # 4. git push
        if should_push:
            print("  📤 正在执行 git push...")
            subprocess.run(['git', 'push'], cwd=repo_path, check=True)
            print("  🎉 Push 成功！")
            
    except subprocess.CalledProcessError as e:
        print(f"  ❌ Git 操作失败，已自动跳过提交: {e}")

def main():
    print("=" * 60)
    print("        🤖 Android 项目依赖版本一键自动升级脚本 🤖")
    print("=" * 60)

    # 1. 交互输入获取版本号（直接回车留空代表不修改该项）
    target_gradle = input("1️⃣  请输入新的 Gradle 版本号 (留空不修改): ").strip()
    target_vcl = input("2️⃣  请输入新的 VCL 插件版本号 (留空不修改): ").strip()
    
    if not target_gradle and not target_vcl:
        print("❌ 未输入任何需要修改的目标版本号，程序退出。")
        return

    # 2. 询问是否要执行 git 提交
    git_input = input("3️⃣  检测到更新后是否执行 Git 提交 (含 Push)? (y/N): ").lower()
    should_git = git_input in ['y', 'yes']

    # 获取当前工作目录
    base_dir = os.path.abspath('.')
    print(f"\n🔍 开始扫描当前目录下的第一级子目录...")
    
    # 3. 遍历当前目录下的所有一级目录
    subdirs = [os.path.join(base_dir, d) for d in os.listdir(base_dir) if os.path.isdir(os.path.join(base_dir, d))]
    
    if not subdirs:
        print("❌ 当前目录下没有发现任何子目录。")
        return

    # 4. 逐个目录审查与处理
    for subdir in sorted(subdirs):
        dir_name = os.path.basename(subdir)
        print(f"\n📁 正在检查子目录: [{dir_name}]")

        # 验证是否为含有远程连接的 Git 仓库
        remote_url = get_git_remote_url(subdir)
        if not remote_url:
            print(f"  ⚠️  [{dir_name}] 不是一个合法的、有关联远程地址的 Git 仓库，跳过。")
            continue
        
        print(f"  🚀 发现 Git 远程仓库: {remote_url}")
        print(f"  🔎 开始检索配置文件...")
        updated_files = []
        gradle_changed = False
        vcl_changed = False

        # 5. 执行修改逻辑
        # A. 修改 Gradle Wrapper
        if target_gradle:
            wrapper_file = find_file_in_dir(subdir, 'gradle-wrapper.properties')
            if wrapper_file:
                changed, _ = update_gradle_version(wrapper_file, target_gradle)
                if changed:
                    gradle_changed = True
                    updated_files.append(wrapper_file)
            else:
                print("  ℹ️  未找到 gradle-wrapper.properties 文件。")

        # B. 修改 Settings.gradle / .kts
        if target_vcl:
            settings_file = find_file_in_dir(subdir, 'settings.gradle.kts') or find_file_in_dir(subdir, 'settings.gradle')
            if settings_file:
                changed, _ = update_vcl_version(settings_file, target_vcl)
                if changed:
                    vcl_changed = True
                    updated_files.append(settings_file)
            else:
                print("  ℹ️  未找到 settings.gradle(.kts) 文件。")

        # 6. 顺应输入要求，按需触发 Git 提交
        if should_git and updated_files:
            print(f"  ⚡ 发现文件变更，正在为 [{dir_name}] 准备 Git 提交...")
            run_git_operations(
                repo_path=subdir,
                updated_files=updated_files,
                vcl_ver=target_vcl if vcl_changed else None,
                gradle_ver=target_gradle if gradle_changed else None,
                should_push=should_git
            )
        elif updated_files:
            print("  ℹ️  文件已修改，根据您的选择，不进行 Git 提交。")
        else:
            print("  ✅ 该项目无需任何变更。")
            
        print("-" * 50)

    print("\n🏁 所有一级目录扫描并处理完毕！")

if __name__ == "__main__":
    main()