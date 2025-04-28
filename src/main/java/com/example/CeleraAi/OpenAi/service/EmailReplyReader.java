package com.example.CeleraAi.OpenAi.service;



import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import jakarta.mail.*;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailReplyReader {
    private final String username = "mg.aangel10@gmail.com";
    private final String password = "immaxvpehqvldfmo";

    private final OpenAIService openAiService;
    private final UsuarioRepo usuarioRepo;

    // Se ejecuta cada 15 min

    public void revisarRespuestas() {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");

            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", username, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] mensajes = inbox.getMessages();
            int totalMensajes = mensajes.length;
            int ultimosN = 20; // puedes subirlo a 50 si quieres

            for (int i = totalMensajes - 1; i >= Math.max(totalMensajes - ultimosN, 0); i--) {
                Message mensaje = mensajes[i];

                String asunto = mensaje.getSubject();
                String contenido = mensaje.getContent().toString();

                Address[] froms = mensaje.getFrom();
                if (froms != null && froms.length > 0) {
                    String emailRemitente = froms[0].toString();

                    // ✅ Extraer solo el correo, si viene con nombre (ej: "Nombre <correo>")
                    if (emailRemitente.contains("<") && emailRemitente.contains(">")) {
                        emailRemitente = emailRemitente.substring(
                                emailRemitente.indexOf("<") + 1,
                                emailRemitente.indexOf(">")
                        );
                    }

                    System.out.println("📨 Respuesta recibida de: " + emailRemitente);
                    System.out.println("📬 ASUNTO del mensaje: " + asunto);


                    // ✅ FILTRO: que el asunto sea de resumen y el remitente válido
                    if (asunto != null && asunto.contains("[BIZYVEL-AI]")) {
                        Usuario usuario = usuarioRepo.findByEmailConNegocios(emailRemitente);
                        if (usuario != null && !usuario.getNegocios().isEmpty()) {
                            UUID idNegocio = usuario.getNegocios().get(0).getId();
                            //openAiService.generarRecomendacionConIA(contenido, idNegocio);
                            System.out.println("🧠 Comando procesado correctamente.");
                        } else {
                            System.out.println("❌ Usuario no encontrado o sin negocios.");
                        }
                    } else {
                        System.out.println("⛔ Ignorado: no es respuesta a un resumen BIZYVEL.");
                    }
                }

                mensaje.setFlag(Flags.Flag.SEEN, true); // ✅ Lo marcamos como leído

            }

            inbox.close(false);
            store.close();

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}