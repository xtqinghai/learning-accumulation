✅ 所有库「用途分类速查表」（精华总结，建议收藏）
1. 视觉 / 音视频核心
   OpenCV（计算机视觉）、FFmpeg（音视频全处理）
2. 硬件相机 / 采集
   libdc1394（工业火线相机）、PGR FlyCapture（灰点工业相机）、OpenKinect（Kinect 体感相机）、videoInput（Windows 视频采集）
3. 图像识别 / OCR/AR
   ARToolKitPlus（AR 增强现实）、Leptonica（图像预处理）、Tesseract（OCR 文字识别）
4. 科学计算
   GSL（开源科学计算）、MKL（Intel 高性能数学库）
5. 深度学习 / 异构计算
   CUDA（GPU 加速）、Caffe（视觉深度学习）、MXNet（跨设备深度学习）、TensorFlow（通用深度学习）
6. 底层开发 / 编译器
   LLVM（编译器基础设施）
7. 科学数据存储
   HDF5（海量高维数据存储）
8. 系统底层
   各类系统原生 API（Windows/Linux/macOS 底层操作）
   补充：为什么 Java 开发者一定要通过 JavaCPP 用这些库？
   这些库全部是C/C++ 原生编写，性能是纯 Java 实现的10~100 倍（尤其是视觉、音视频、科学计算、AI 领域）；
   这些库都是工业级成熟库，经过数十年的打磨，稳定性、兼容性远超自研的 Java 框架；
   JavaCPP 完美封装了这些库，无需手写 JNI 胶水代码、无需编译 C++ 文件，Java 开发者直接引入依赖就能调用，零门槛。
   希望这份详细的介绍能帮你理清这些库的用途和价值！😊
