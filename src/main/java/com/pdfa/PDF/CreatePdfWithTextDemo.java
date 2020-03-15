package com.pdfa.PDF;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

public class CreatePdfWithTextDemo {
	
	public static void main(String[] args) throws IOException, TransformerException {
        String filename = "src/sample.pdf";
        String message = "This is a sample PDF document created using PDFBox.";
        
        PDDocument doc = new PDDocument();
        try {
            PDPage page = new PDPage();
            doc.addPage(page);
            
            PDFont font = PDType0Font.load(doc, CreatePdfWithTextDemo.class.
            		getResourceAsStream("/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"));
 
            PDPageContentStream contents = new PDPageContentStream(doc, page);
            contents.beginText();
            contents.setFont(font, 30);
            contents.newLineAtOffset(50, 700);
            contents.showText(message);
            contents.endText();
            contents.close();
            
            
            
            XMPMetadata xmp = XMPMetadata.createXMPMetadata();
            PDDocumentCatalog catalogue = doc.getDocumentCatalog();
            Calendar cal =  Calendar.getInstance();          

            try
            {
                DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
               // dc.setTitle(file);
                dc.addCreator("My APPLICATION Creator");
                dc.addDate(cal);

                PDFAIdentificationSchema id = xmp.createAndAddPFAIdentificationSchema();
                id.setPart(3);  //value => 2|3
                id.setConformance("B"); // value => A|B|U

                XmpSerializer serializer = new XmpSerializer();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                serializer.serialize(xmp, baos, true);

                PDMetadata metadata = new PDMetadata(doc);
                metadata.importXMPMetadata(baos.toByteArray());                
                catalogue.setMetadata(metadata);
            }
            catch(BadFieldValueException e)
            {
                throw new IllegalArgumentException(e);
            }
            
            
            
            
            doc.save(filename);
        }
        finally {
            doc.close();
        }
    }

}
