package com.example.CeleraAi.OpenAi.service;




import com.example.CeleraAi.Negocio.repositorio.NegocioRepo;
import com.example.CeleraAi.users.model.Usuario;
import com.example.CeleraAi.users.repositorio.UsuarioRepo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;




import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class EmailSender {
    private final NegocioRepo negocioRepo;
    private final JavaMailSender mailSender;
    private final OpenAIService resumenDiarioService;
    private final UsuarioRepo usuarioRepo;
    private final ResumenPDFGenerator resumenPDFGenerator;
    private final GeneradorResumenPDF generadorPDF;

    // 🕗 Enviar todos los días a las 20:00 (hora servidor)

    @Scheduled(cron = "0 21 00 * * *")
    public void enviarResumenADuenos() {
        List<Usuario> usuarios = usuarioRepo.obtenerTodosConNegocios();

        System.out.println("entra en enviarResumenes a dueños");


        for (Usuario usuario : usuarios) {
            System.out.println("Usuario: " + usuario.getEmail() + " tiene negocios: " + usuario.getNegocios().size());

            usuario.getNegocios().forEach(negocio -> {
                System.out.println("Enviando resumen para negocio: " + negocio.getNombre());

                String resumen = resumenDiarioService.generarResumenTexto(negocio.getId());
                byte[] pdf = generadorPDF.generarResumenEnPDF(negocio.getId());

                enviarCorreoConAdjunto(
                        usuario.getEmail(),
                        "📊 Resumen diario de tu negocio",
                        resumen,
                        pdf,
                        "resumen.pdf"
                );

                System.out.println("✅ Correo enviado a: " + usuario.getEmail());
            });
        }

    }

    public void enviarCorreoConAdjunto(String to, String asunto, String texto, byte[] pdf, String nombreArchivo) {
        try {
            System.out.println("entra en enviar correo con pdf");
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[BIZYVEL-AI] 📊 Resumen diario de tu negocio");

            helper.setText(texto);

            // Adjuntar el PDF
            DataSource dataSource =  new ByteArrayDataSource(pdf, "application/pdf");
            helper.addAttachment(nombreArchivo, dataSource);

            mailSender.send(mensaje);
            System.out.println("lo ha enviado");
            System.out.println("✅ Correo con adjunto enviado a " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Error al enviar el correo con adjunto: " + e.getMessage());
        }
    }

}