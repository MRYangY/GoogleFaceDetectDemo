package com.example.root.cameramodule;

public class Camera2Api {
    private static volatile Camera2Api CAMERA2API_INSTANCE = null;


    private Camera2Api() {

    }

    public static Camera2Api getInstance() {
        if (CAMERA2API_INSTANCE == null) {
            synchronized (Camera2Api.class) {
                if (CAMERA2API_INSTANCE == null) {
                    CAMERA2API_INSTANCE = new Camera2Api();
                }
            }
        }
        return CAMERA2API_INSTANCE;
    }


}
