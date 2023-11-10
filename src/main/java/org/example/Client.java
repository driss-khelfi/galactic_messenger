package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class Client implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;
    private String nom;
    private String hashedPassword;

    @Override
    public void run() {

        try {
            client = new Socket("localhost", 1234);
            // on envoie le message au serveur
            out = new PrintWriter(client.getOutputStream(), true);
            // on récupère le message du serveur
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            // on demande à l'utilisateur de se connecter ou de s'inscrire
            String commande = customerLaunch();

            out.println(commande + " " + nom + " " + hashedPassword);


            if (commande.equals("/login")) {
                System.out.println("vous êtes connecté");

            } else if (commande.equals("/register")) {
                System.out.println("vous êtes inscrit et connecté !\n");

            } else if (commande.equals("/quit")) {
                System.out.println("vous avez quitté le chat");
                shutdown();
            } else if (commande.equals("/help")) {
                System.out.println("/login nom_d'utilisateur mot_de_passse : pour vous connecter");
                System.out.println("/register nom_d'utilisateur mot_de_passs : pour vous inscrire");
                System.out.println("/quit : pour quitter");
            } else {
                System.out.println("Vous n'avez pas choisi une commande existantante");
            }

            // crée un nouveau thread pour gérer les entrées clavier
            InputHandler inHandler = new InputHandler();
            // crée un nouveau thread
            Thread t = new Thread(inHandler);
            // lance le thread
            t.start();
            // on récupère le message du serveur
            String message;

            // tant que le client n'a pas envoyé /quit
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            // TO DO: handle exception
        }
    }

    public String customerLaunch(){
        System.out.println("voulez vous vous connecter ( /login nom_d'utilisateur mot_de_passse) \n ou \nvous inscrire ( /register nom_d'utilisateur mot_de_passs) ?");

        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();

        // on parse la chaine de caractère pour récupérer les différents éléments
        Scanner parser = new Scanner(str);

        String choix = parser.next();
        String nom = parser.next();
        String mdp = parser.next();

        // Hasher le mot de passe avec BCrypt
        String hashedPassword = BCrypt.withDefaults().hashToString(12, mdp.toCharArray());

        this.nom = nom;
        this.hashedPassword = hashedPassword;

        System.out.println("Vous avez choisi la commande : " + choix + "; le nom : " + nom + "; le mot de passe : " + hashedPassword);

        // tant que l'utilisateur n'a pas choisi une commande valide
        while (choix.charAt(0) != '/') {

            System.out.println("Vous n'avez pas choisi une commande valide");
            System.out.println("voulez vous vous connecter ( /login nom_d'utilisateur mot_de_passs) \n ou \nvous inscrire ( /register nom_d'utilisateur mot_de_passs) ?");

            str = scanner.nextLine();
            System.out.println("Vous avez choisi : " + str);

            parser = new Scanner(str);

            choix = parser.next();
            nom = parser.next();
            mdp = parser.next();
            hashedPassword = BCrypt.withDefaults().hashToString(12, mdp.toCharArray());

            System.out.println("Vous avez choisi la commande : " + choix + "; le nom : " + nom + "; le mot de passe : " + hashedPassword);
        }

        return choix;
    }

    // ferme les flux et le socket
    public void shutdown() throws IOException {
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            // ignore
        }
    }

    // classe interne pour gérer les entrées clavier
    class InputHandler implements Runnable {
        @Override
        public void run() {
            try {

                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = inReader.readLine();
                    if (message.equals("/quit")) {
                        out.println(message);
                        shutdown();
                    } else {
                        out.println(message);
                    }
                }
                inReader.close(); // Move this line outside the while loop
            } catch (IOException e) {
                try {
                    shutdown();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
