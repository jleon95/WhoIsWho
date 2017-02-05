/*
From:
https://github.com/zoraidacallejas/ConversationalInterface/tree/master/chapter7/TalkBot
 */

package com.npi.whoiswho.pandora;

public enum PandoraErrorCode {

    UNKNOWN, //The cause of the error is unknown
    NOMATCH, //There is no valid response coded in the AIML for that particular query
    ID,  //App id, user key or robot name are invalid
    IDORHOST,  //Either ID error or the host (pandora website) is not reachable
    CONNECTION, //Internet connection error
    PARSE //Error parsing the robot response

}
