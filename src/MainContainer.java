import jade.wrapper.ContainerController;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;


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
            prof.setParameter(Profile.CONTAINER_NAME, containerName);
            prof.setParameter(Profile.MAIN_HOST, host);
            prof.setParameter(Profile.MAIN_PORT, port);
            prof.setParameter(Profile.MAIN, "true");
            prof.setParameter(Profile.GUI, "true");

            // create a main agent container
            this.container = rt.createMainContainer(prof);
            rt.setCloseVM(true);

        }

        public void startAgentInPlatform(String name, String classpath) {
            try {
                AgentController ac = container.createNewAgent(name, classpath, new Object[0]);
                ac.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void main(String[] args) {
            MainContainer a = new MainContainer();

            try {

                a.initMainContainerInPlatform("localhost", "9888", "MainContainer");

                // First Open Central Agent - Lider
                a.startAgentInPlatform("Lider", "Lider");

                // Provide some time for Agent to register in services

                Thread.sleep(500);

                int limit_player = 5; // Limit number of Player

                // Start agents Taxis!
                for (int n = 0; n < limit_player; n++) {
                    a.startAgentInPlatform("Jogador" + n, "Jogador");
                }

                // Provide some time for Agents to register in services
                Thread.sleep(1000);


            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
}

