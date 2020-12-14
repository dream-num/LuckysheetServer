package com.xc.luckysheet.websocket;


import lombok.extern.slf4j.Slf4j;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.Query;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * @author Administrator
 */
@Slf4j
public class IpAndPortUtil {

    /**
     * 获取ip地址及端口port
     * @return
     * @throws MalformedObjectNameException
     * @throws NullPointerException
     * @throws UnknownHostException
     */
    public static String getIpAddressAndPort() {
      try{
          MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
          Set<ObjectName> objectNames = beanServer.queryNames(new ObjectName("*:type=Connector,*"),
                  Query.match(Query.attr("protocol"), Query.value("HTTP/1.1")));
          String host = InetAddress.getLocalHost().getHostAddress();
          String port = objectNames.iterator().next().getKeyProperty("port");
          //String ipadd = "http" + "://" + host + ":" + port;
          String ipadd = host + ":" + port;
          //System.out.println(ipadd);
          return ipadd;
      }catch (Exception ex){
    	  log.error("getIpAddressAndPort+EXCEPTION:"+ex);
          return "";
      }
    }


    /**
     * 获得Linux下的WebLogic的IP
     * @return
     */
    public static String getIpWeblogic(){
        Context ctx=null;
        String listenAddr=null;
        String port=null;
        try {
            ctx = new InitialContext();
            MBeanServer tMBeanServer = (MBeanServer) ctx.lookup("java:comp/env/jmx/runtime");
            ObjectName tObjectName = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
            ObjectName serverrt = (ObjectName) tMBeanServer.getAttribute(tObjectName, "ServerRuntime");
            port = String.valueOf(tMBeanServer.getAttribute(serverrt, "ListenPort"));
            listenAddr =(String)tMBeanServer.getAttribute(serverrt,"ListenAddress");
            String[] tempAddr = listenAddr.split("/");
            if(tempAddr.length == 1){
                listenAddr = tempAddr[0];
            } else if(tempAddr[tempAddr.length - 1].trim().length() != 0){
                listenAddr = tempAddr[tempAddr.length - 1];
            } else if (tempAddr.length > 2){
                listenAddr=tempAddr[tempAddr.length-2];
            }
        } catch (Exception e) {
          log.error("getIpWeblogic+EXCEPTION:"+e);
        }
        return listenAddr+":"+port;

    }
}
