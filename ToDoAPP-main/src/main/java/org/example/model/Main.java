package org.example.model;

import com.formdev.flatlaf.FlatLightLaf;
import org.example.controller.Controller;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Controller.startApp();
    }
}
