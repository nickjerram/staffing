package org.camra.staffing.controller;

import java.util.ArrayList;
import java.util.List;

public class ResponseDTO {

    public boolean success;
    public boolean error;
    public List<String> errors = new ArrayList<>();
    public List<String> messages = new ArrayList<>();
}
