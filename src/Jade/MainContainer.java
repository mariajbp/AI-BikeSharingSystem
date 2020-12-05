package Jade;

import Util.APE;
import Util.Posicao;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.ObjectInputStream;
import java.util.Random;

public class MainContainer {

    Runtime rt;
    ContainerController container;

    public ContainerController initContainerInPlatform(String host, String port, String containerName) {
        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance();

        // Create a Profile, where the launch arguments are stored
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.CONTAINER_NAME, containerName);
        profile.setParameter(Profile.MAIN_HOST, host);
        profile.setParameter(Profile.MAIN_PORT, port);
        // create a non-main agent container
        ContainerController container = rt.createAgentContainer(profile);
        return container;
    }

    public void initMainContainerInPlatform(String host, String port, String containerName) {

        // Get the JADE runtime interface (singleton)
        this.rt = Runtime.instance();

        // Create a Profile, where the launch arguments are stored
        Profile prof = new ProfileImpl();

        prof.setParameter(Profile.MAIN_HOST, host);
        prof.setParameter(Profile.MAIN_PORT, port);
        prof.setParameter(Profile.CONTAINER_NAME, containerName);
        prof.setParameter(Profile.MAIN, "true");
        prof.setParameter(Profile.GUI, "true");

        // create a main agent container
        this.container = rt.createMainContainer(prof);
        rt.setCloseVM(true);

    }

    public void startAgentInPlatform(String name, String classpath, Object[] args) {
        try {
            AgentController ac = container.createNewAgent(name, classpath, args);
            ac.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MainContainer a = new MainContainer();

        try {
            a.initMainContainerInPlatform("localhost", "9885", "MainContainer");

            // Example of Container Creation (not the main container)
            // Create 3 different containers (separated environments) inside the
            // Main container

            Object[] args_input = new Object[] { "Container1" };

            ContainerController newcontainer0 = a.initContainerInPlatform("localhost", "9886", "Container0");
            ContainerController newcontainer1 = a.initContainerInPlatform("localhost", "9887",
                    args_input[0].toString());

            AgentController sta1 = newcontainer1.createNewAgent("Sta1", "Agents.AgenteEstacao", new Object[] {});

            AgentController user1 = newcontainer1.createNewAgent("User1", "Agents.AgenteUtilizador", new Object[] {});
            AgentController mon1 = newcontainer1.createNewAgent("Mon1", "Agents.AgenteMonitor", new Object[] {});
            AgentController inter1 = newcontainer1.createNewAgent("Inter1", "Agents.AgenteInterface", new Object[] {});
            sta1.start();
            mon1.start();


            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            user1.start();
            inter1.start();


            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }



        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }
}