package com.cloudring.magic.camera.photograph;

//回调状态类型及状态接口  type：0视频1相机 state：0打开 1关闭   type即模式
//mode 模式：0 视频模式  1相机模式
public interface IaudioState {


    void mode(int type);

}
