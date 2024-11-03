import os
import subprocess

def is_gradle_project(directory):
    # 检查目录中是否包含 gradlew 脚本，build.gradle 或 settings.gradle 文件
    return any(os.path.isfile(os.path.join(directory, filename)) for filename in ['gradlew', 'build.gradle', 'settings.gradle'])

def clean_gradle_project(directory):
    # 进入该目录并执行 `./gradlew clean` 命令
    gradlew_path = os.path.join(directory, 'gradlew')
    if os.path.isfile(gradlew_path):  # 确保 gradlew 存在
        print(f"Cleaning Gradle project: {directory}")
        subprocess.run(["./gradlew", "clean"], cwd=directory)

def traverse_directory(root_directory):
    for dirpath, dirnames, filenames in os.walk(root_directory):
        # 如果当前目录是 Gradle 项目，执行清理并跳过子目录
        if is_gradle_project(dirpath):
            clean_gradle_project(dirpath)
            dirnames.clear()  # 清空 dirnames，避免遍历子目录
        # 忽略遍历 build 目录
        if 'build' in dirnames:
            dirnames.remove('build')

# 执行遍历，从当前目录开始
traverse_directory(os.getcwd())
