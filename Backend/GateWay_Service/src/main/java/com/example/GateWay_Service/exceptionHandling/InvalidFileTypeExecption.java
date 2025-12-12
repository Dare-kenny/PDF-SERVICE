package com.example.GateWay_Service.exceptionHandling;

public class InvalidFileTypeExecption extends RuntimeException{
    //runtime exception to signal wrong file type
    public InvalidFileTypeExecption(String message){
        super(message);
    }
}
