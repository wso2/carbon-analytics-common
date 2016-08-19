/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// var registerCallBackforPush;

(function() {

    var callback;

    /**
     * TODO path needs to be updated if jaggery files are moved in the portal
     * @param providerConfig
     * @param schema
     */
    registerCallBackforPush = function(providerConfig, schema, _callback) {
        var hostname = window.parent.location.hostname;
        var port = window.parent.location.port;

        var url = 'wss://'+ hostname +':'+port+'/portal/websocket/server.jag';
        ws = new WebSocket(url);
        setTimeout(function() {
            ws.send(providerConfig.streamName);
        }, 500);
        ws.onmessage = function(event) {
            _callback(eval(event.data));
        };
    };
}());

