package Jade;

import Util.APE;
import Util.ConfigVars;
import Util.Posicao;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.List;
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
            ContainerController newcontainer1 = a.initContainerInPlatform("localhost", "9887",
                    args_input[0].toString());
            List<AgentController> all= new ArrayList<>();
            List<Posicao> stations = new ArrayList<>();
            for(int i = 0; i< ConfigVars.STATION_NUM; i++){
                Random r =new Random();
                Posicao ps =  new Posicao(r.nextInt(ConfigVars.MAP_SIZE),r.nextInt(ConfigVars.MAP_SIZE));
                stations.add(ps);
                Object[] staArgs= new Object[]{
                            new APE(ps), ps, ConfigVars.CAP_INIT
                    };

                all.add(
                             newcontainer1.createNewAgent("Sta"+i, "Agents.AgenteEstacao", staArgs)
                     );}
            for(AgentController ac : all) ac.start();


            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            AgentController mon1 = newcontainer1.createNewAgent("Mon1", "Agents.AgenteMonitor", new Object[] {});
            AgentController inter1 = newcontainer1.createNewAgent("Inter1", "Agents.AgenteInterface", new Object[] {});

            mon1.start();

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            all= new ArrayList<>();
            for(int i=0;i<ConfigVars.INIT_USERS;i++){
                Random r =new Random();
                all.add(
                        newcontainer1.createNewAgent("User"+i, "Agents.AgenteUtilizador", new Object[]{
                                stations.get(r.nextInt(stations.size()-1)),
                                new Posicao(r.nextInt(ConfigVars.MAP_SIZE),r.nextInt(ConfigVars.MAP_SIZE)) })
                );}
            for(AgentController ac : all) ac.start();


            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

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