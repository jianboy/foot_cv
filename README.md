# 足底纹理生成器 (Foot Pattern Generator)

这个Java程序使用OpenCV处理足底图像并生成具有足底纹理的金色足印图像。

## 项目描述

该项目读取足底图像，从背景中分离出足底，然后创建带有清晰足底纹理的金色图案。

## 功能特点

- 高级足底检测与分割
- 背景移除
- 足底纹理增强与保留
- 金色足印生成
- 圆形背景设计

## 如何构建与运行

1. 确保安装了Java 11+和Maven
2. 构建项目：`mvn clean package`
3. 运行程序：`mvn exec:java`

程序将：
1. 读取输入图像：`/workspace/foot_cv/1.png`
2. 处理图像生成足底纹理图案
3. 保存结果到：`/workspace/foot_cv/result.png`

## 依赖

- OpenCV 4.5.1-2 (通过org.openpnp:opencv)

