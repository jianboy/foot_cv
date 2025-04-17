#提取脚掌生成脚印 by Xiao.DU

import os
import cv2
from PIL import Image
import numpy as np

# 设置工作目录
os.chdir("/workspace/foot_cv/")

# 创建结果目录
result_dir = "./Demo/result"
if not os.path.exists(result_dir):
    os.makedirs(result_dir)

# 遍历 Demo 目录下的所有图片文件
image_files = [f for f in os.listdir("./Demo") if f.endswith(".png") or f.endswith(".jpg")]

for filename in image_files:
    print(f"处理图片: {filename}")

    # 读取图片，支持透明通道
    image = cv2.imread("./Demo/" + filename, cv2.IMREAD_UNCHANGED)

    # 检查图像是否有 Alpha 通道
    if image.shape[2] == 4:
        bgr = image[:, :, :3]  # 获取 BGR 通道
        alpha = image[:, :, 3]  # 获取 Alpha 通道
    else:
        raise ValueError("图像没有 Alpha 通道")

    # 将 BGR 图像转换为灰度图像
    gray = cv2.cvtColor(bgr, cv2.COLOR_BGR2GRAY)

    # 定义浮雕效果的卷积核
    kernel = np.array([[-2, -1, 0],
                       [-1,  1, 1],
                       [ 0,  1, 2]])
    kernel = np.array([[-4, -2, 0],
                       [-2,  1, 2],
                       [ 0,  2, 4]])
    kernel = np.array([[-6, -4, 0],
                       [-4,  1, 4],
                       [ 0,  4, 6]])
    kernel = np.array([[-8, -6, 0],
                       [-6,  1, 6],
                       [ 0,  6, 8]])
    kernel = np.array([[-10, -8, 0],
                   [-8,  1, 8],
                   [ 0,  8, 10]])
    # 应用卷积核，生成浮雕效果
    embossed = cv2.filter2D(gray, -1, kernel)

    # 归一化浮雕效果图像
    normalized = cv2.normalize(embossed, None, 0, 255, cv2.NORM_MINMAX)

    # 将灰度图像转换回 BGR
    embossed_bgr = cv2.cvtColor(normalized, cv2.COLOR_GRAY2BGR)

    # 设置前景色和背景色
    foreground_color = np.array([111, 193, 222], dtype=np.uint8)  # 金黄色
    background_color = np.array([0, 0, 0], dtype=np.uint8)        # 黑色

    # 创建浮雕效果的彩色图像
    embossed_color = np.where(embossed_bgr > 128, foreground_color, background_color)

    # 合并处理后的 BGR 图像和原始 Alpha 通道
    result = cv2.merge((embossed_color, alpha))

    # 保存结果
    cv2.imwrite(f"./Demo/result/{filename}_gold.png", result)

    # 删除黑色纹理

    # 读取图片，支持透明通道
    image = cv2.imread(f"./Demo/result/{filename}_gold.png", cv2.IMREAD_UNCHANGED)

    # 检查图像是否有 Alpha 通道
    if image.shape[2] == 4:
        bgr = image[:, :, :3]  # 获取 BGR 通道
        alpha = image[:, :, 3]  # 获取 Alpha 通道
    else:
        raise ValueError("图像没有 Alpha 通道")

    # 分离通道
    bgr = image[:, :, :3]
    alpha = image[:, :, 3]

    # 定义黑色阈值范围 (可以调整以适应不同黑色程度)
    lower_black = np.array([0, 0, 0], dtype=np.uint8)
    upper_black = np.array([30, 30, 30], dtype=np.uint8)  # 允许一定灰度的黑色

    # 创建黑色掩码
    black_mask = cv2.inRange(bgr, lower_black, upper_black)

    # 计算黑色区域的比例
    black_ratio = np.sum(black_mask > 0) / black_mask.size

    # 判断黑色区域是否超过50%
    if black_ratio > 0.5:
        # 设置黑色区域为黄色
        bgr[black_mask > 0] = [111, 193, 222]  # 黄色
        # 将其他区域的 Alpha 设置为 0 (透明)
        alpha[black_mask == 0] = 0
    else:
        # 将黑色区域的 Alpha 设置为 0 (透明)
        alpha[black_mask > 0] = 0

    # 合并 BGR 和更新后的 Alpha 通道
    final_result = cv2.merge((bgr, alpha))

    # 保存最终结果
    cv2.imwrite(f"./Demo/result/{filename}_result.png", final_result)

print("所有图片处理完成。")

# 将所有result.png转换为平面图
def convert_to_flat_view():
    print("开始将result.png图像转换为平面图...")
    
    # 获取所有result.png文件
    result_files = [f for f in os.listdir(result_dir) if f.endswith("_result.png")]
    
    for filename in result_files:
        print(f"转换图片为平面图: {filename}")
        
        # 读取图片
        image_path: str = os.path.join(result_dir, filename)
        image: cv2.Mat | np.ndarray[os.Any, np.dtype[np.integer[os.Any] | np.floating[os.Any]]] = cv2.imread(image_path, cv2.IMREAD_UNCHANGED)
        
        # 检查图像是否有Alpha通道
        if image.shape[2] == 4:
            # 分离通道
            bgr = image[:, :, :3]
            alpha = image[:, :, 3]
            
            # 创建纯白色背景
            white_bg = np.ones_like(bgr) * 255
            
            # 为非透明区域创建掩码
            mask = alpha > 0
            mask = np.stack([mask, mask, mask], axis=2)
            
            # 将足迹图像叠加到白色背景上
            flat_view = np.where(mask, bgr, white_bg)
            
            # 保存平面图
            flat_view_path = os.path.join(result_dir, f"{filename.replace('_result.png', '_flat.png')}")
            cv2.imwrite(flat_view_path, flat_view)
        else:
            print(f"图像 {filename} 没有Alpha通道，跳过处理")
    
    print("所有图片平面图转换完成。")

# 执行平面图转换
convert_to_flat_view()