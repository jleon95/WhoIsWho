/*
From:
https://github.com/zoraidacallejas/ConversationalInterface/tree/master/chapter7/TalkBot
 */

package com.npi.whoiswho.pandora;

public class PandoraException extends Throwable {

    private PandoraErrorCode errorCode=PandoraErrorCode.UNKNOWN;

    PandoraException(PandoraErrorCode c){
        setErrorCode(c);
    }

    public void setErrorCode(PandoraErrorCode code){
        errorCode = code;
    }

    public PandoraErrorCode getErrorCode(){
        return errorCode;
    }
}
