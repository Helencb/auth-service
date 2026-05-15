package helen.com.authservice.security.mfa;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Component
public class QRCodeGenerator {
    public String generateQRCode(String text){
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    250,
                    250
            );
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(
                    matrix,
                    "PNG",
                    outputStream
            );
            return Base64.getEncoder()
                    .encodeToString(outputStream.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Error generating QRCode");
        }
    }
}
