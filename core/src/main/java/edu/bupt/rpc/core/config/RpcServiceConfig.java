package edu.bupt.rpc.core.config;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    private String version = "";
    private String group = "";

    private Object service;

    public String getRpcServiceName() {
        return String.join("-",this.getServiceName() , this.getGroup() ,this.getVersion());
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
