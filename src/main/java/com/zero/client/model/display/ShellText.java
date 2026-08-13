package com.zero.client.model.display;

import com.zero.Zero;
import com.zero.client.model.bedrock.BedrockModel;
import com.zero.server.file.FileList;
import com.zero.server.type.InfoType;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ShellText extends TxtRead {
    private Vector3f initialVelocity = new Vector3f(8.0f, 5.0f, -0.5f);
    private Vector3f randomVelocity = new Vector3f(2.5f, 1.5f, 0.25f);
    private Vector3f acceleration = new Vector3f(0, -20f, 0);
    private Vector3f angularVelocity = new Vector3f(-720, -720, 90);
    private float livingTime = 1.0f;
    private BedrockModel shellModel;
    private String shellTexture;

    public ShellText(List<String> text, FileList fileList) {
        super(text, fileList);
    }

    @Override
    public void read(String[] split, FileList fileList) {
        initialVelocity = Read(split, "initialVelocity", initialVelocity, fileList);
        randomVelocity = Read(split, "randomVelocity", randomVelocity, fileList);
        acceleration = Read(split, "acceleration", acceleration, fileList);
        angularVelocity = Read(split, "angularVelocity", angularVelocity, fileList);
        livingTime = Read(split, "livingTime", livingTime, fileList);
        String shell = Read(split, "shell", "");
        if (!shell.isEmpty()) {
            this.shellModel = new BedrockModel(Zero.server.loadModel(fileList, "shell/" + shell));
        }
        shellTexture = Read(split, "shellTexture", shellTexture);
    }

    private Vector3f Read(String[] split, String name, Vector3f currentValue, FileList fileList) {
        List<String> list = Read(split, name, new ArrayList<>());
        if (!list.isEmpty()) {
            try {
                currentValue = new Vector3f(Float.parseFloat(list.get(0)), Float.parseFloat(list.get(1)), Float.parseFloat(list.get(2)));
            } catch (NumberFormatException e) {
                InfoType.error(fileList, name + ": 格式不正确传入值非小数点或整数");
            }
        }
        return currentValue;
    }

    public Vector3f getInitialVelocity() {
        return initialVelocity;
    }

    public Vector3f getRandomVelocity() {
        return randomVelocity;
    }

    public Vector3f getAcceleration() {
        return acceleration;
    }

    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }

    public float getLivingTime() {
        return livingTime;
    }

    public BedrockModel getShellModel() {
        return shellModel;
    }

    public String getShellTexture() {
        return shellTexture;
    }
}
