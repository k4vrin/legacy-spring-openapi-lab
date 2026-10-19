package dev.kavrin.legacyorders.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;

public final class LocalTomcat {
    private LocalTomcat() { }

    public static Tomcat start(int port) throws Exception {
        Path base = Files.createTempDirectory(Paths.get("target"), "tomcat-");
        Files.createDirectories(base.resolve("webapps"));
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(base.toAbsolutePath().toString());
        tomcat.setHostname("localhost");
        tomcat.setPort(port);
        // This API-only WAR has no JSPs and needs no container-generated JSP servlet.
        tomcat.setAddDefaultWebXmlToWebapp(false);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        Context context = tomcat.addWebapp("/legacy-orders", Paths.get("target/legacy-orders.war").toAbsolutePath().toString());
        // exec:java uses a Maven plugin classloader, which differs from the JVM system classloader.
        context.setParentClassLoader(LocalTomcat.class.getClassLoader());
        // Inspect the deployed WAR, not the launcher's Maven/test dependency classpath.
        ((StandardJarScanner) context.getJarScanner()).setScanClassPath(false);
        tomcat.start();
        if (!context.getState().isAvailable()) {
            tomcat.stop();
            tomcat.destroy();
            throw new IllegalStateException("The orders WAR failed to start; inspect the Tomcat log");
        }
        return tomcat;
    }

    public static void main(String[] args) throws Exception {
        Tomcat tomcat = start(Integer.getInteger("demo.http.port", 8080));
        System.out.println("Orders: http://localhost:" + tomcat.getConnector().getLocalPort()
                + "/legacy-orders/api/orders");
        System.out.println("Docs, when enabled: /legacy-orders/api/docs/index.html");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { tomcat.stop(); tomcat.destroy(); }
            catch (Exception exception) { exception.printStackTrace(); }
        }));
        tomcat.getServer().await();
    }
}
