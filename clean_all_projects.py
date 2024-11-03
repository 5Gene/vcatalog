import os
import subprocess
from pathlib import Path

def is_gradle_project(directory):
    """
    检查目录是否为Gradle项目
    通过查找build.gradle或settings.gradle文件来判断
    """
    gradle_files = ['build.gradle', 'settings.gradle', 'build.gradle.kts', 'settings.gradle.kts']
    return any(os.path.exists(os.path.join(directory, file)) for file in gradle_files)

def find_gradle_projects(start_path='.'):
    """
    递归查找所有Gradle项目目录
    """
    gradle_projects = []

    for root, dirs, files in os.walk(start_path):
        # 跳过.gradle和build目录
        dirs[:] = [d for d in dirs if d not in ['.gradle', 'build', '.git']]

        if is_gradle_project(root):
            gradle_projects.append(root)
            # 找到Gradle项目后跳过其子目录
            dirs.clear()

    return gradle_projects

def clean_gradle_project(project_path):
    """
    执行gradle clean命令
    """
    try:
        # 检查是否存在gradlew脚本
        os.chdir(project_path)
        print(f"\n清理项目: {os.getcwd()}")
        # 执行清理命令
        result = subprocess.run(
            ["./gradlew", "clean"],
            capture_output=True,
            text=True
        )

        if result.returncode == 0:
            print(f"✓ 清理成功")
            return True
        else:
            print(f"✗ 清理失败")
            print(f"错误信息: {result.stderr}")
            return False

    except Exception as e:
        print(f"✗ 执行清理时出错: {str(e)}")
        return False

def main():
    # 获取当前目录的绝对路径
    current_dir = os.path.abspath('.')
    print(f"开始在 {current_dir} 中查找Gradle项目...")

    # 查找所有Gradle项目
    gradle_projects = find_gradle_projects(current_dir)

    if not gradle_projects:
        print("未找到Gradle项目")
        return

    print(f"\n找到 {len(gradle_projects)} 个Gradle项目:")
    for project in gradle_projects:
        print(f"- {project}")

    # 执行清理
    print("\n开始清理项目...")
    success_count = 0
    for project in gradle_projects:
        if clean_gradle_project(project):
            success_count += 1

    # 打印总结
    print(f"\n清理完成: {success_count}/{len(gradle_projects)} 个项目清理成功")

if __name__ == "__main__":
    main()
