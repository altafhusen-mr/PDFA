package com.facthacker.PDF;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws FileNotFoundException
    {
    	String resultFile = "src/PDFA-x.PDF";  
        FileInputStream in = new FileInputStream("src/STSIMmatrikulationsbesch.pdf");

        PDDocument doc = new PDDocument();
        try 
        {
            PDPage page = new PDPage();
            doc.addPage(page); 
            doc.setVersion(1.7f);

            /*             
            // A PDF/A file needs to have the font embedded if the font is used for text rendering
            // in rendering modes other than text rendering mode 3.
            //
            // This requirement includes the PDF standard fonts, so don't use their static PDFType1Font classes such as
            // PDFType1Font.HELVETICA.
            //
            // As there are many different font licenses it is up to the developer to check if the license terms for the
            // font loaded allows embedding in the PDF.

            String fontfile = "/org/apache/pdfbox/resources/ttf/ArialMT.ttf"; 
            PDFont font = PDType0Font.load(doc, new File(fontfile));           
            if (!font.isEmbedded())
            {
                throw new IllegalStateException("PDF/A compliance requires that all fonts used for"
                        + " text rendering in rendering modes other than rendering mode 3 are embedded.");
            }
          */ 

            PDPageContentStream contents = new PDPageContentStream(doc, page);
            try 
            {   
                PDDocument docSource = PDDocument.load(in);
                PDFRenderer pdfRenderer = new PDFRenderer(docSource);               
                int numPage = 0;

                BufferedImage imagePage = pdfRenderer.renderImageWithDPI(numPage, 200); 
                PDImageXObject pdfXOImage = LosslessFactory.createFromImage(doc, imagePage);

                contents.drawImage(pdfXOImage, 0,0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
                contents.close();   

            }catch (Exception e) {
                // TODO: handle exception
            }

            // add XMP metadata
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

            // sRGB output intent
            InputStream colorProfile = App.class.getResourceAsStream("/org/apache/pdfbox/resources/icc/ISOcoated_v2_300_bas.icc");
            PDOutputIntent intent = new PDOutputIntent(doc, colorProfile);
            intent.setInfo("sRGB IEC61966-2.1");
            intent.setOutputCondition("sRGB IEC61966-2.1");
            intent.setOutputConditionIdentifier("sRGB IEC61966-2.1");
            intent.setRegistryName("http://www.color.org");

            catalogue.addOutputIntent(intent);  
            catalogue.setLanguage("en-US");

            PDViewerPreferences pdViewer =new PDViewerPreferences(page.getCOSObject());
            pdViewer.setDisplayDocTitle(true);; 
            catalogue.setViewerPreferences(pdViewer);

            PDMarkInfo  mark = new PDMarkInfo(); // new PDMarkInfo(page.getCOSObject()); 
            PDStructureTreeRoot treeRoot = new PDStructureTreeRoot(); 
            catalogue.setMarkInfo(mark);
            catalogue.setStructureTreeRoot(treeRoot);           
            catalogue.getMarkInfo().setMarked(true);

            PDDocumentInformation info = doc.getDocumentInformation();               
            info.setCreationDate(cal);
            info.setModificationDate(cal);            
            info.setAuthor("My APPLICATION Author");
            info.setProducer("My APPLICATION Producer");;
            info.setCreator("My APPLICATION Creator");
            info.setTitle("PDF title");
            info.setSubject("PDF to PDF/A{2,3}-{A,U,B}");           

            doc.save(resultFile);
        }catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

    }
}
