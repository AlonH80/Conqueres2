import generated.*;

import java.io.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


public class FileHandling {
    public static String csfFilesPath = "csf_files";
    public String fileName;


    public FileHandling(String fileName){
        setFileName(fileName);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName(){
        return fileName;
    }

    public GameDescriptor fileToGame(){
        try {
            File file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(GameDescriptor.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (GameDescriptor) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static GameDescriptor fileToGame(String fileName) throws FileNotFoundException,JAXBException{
        GameDescriptor obj;
        File file = new File(fileName);
        if (!file.exists()){
            throw (new FileNotFoundException());
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(GameDescriptor.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        obj = (GameDescriptor) jaxbUnmarshaller.unmarshal(file);
        return obj;
    }

    public void gameToFile(GameDescriptor obj){
        try {

            File file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(obj, file);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static void gameToXMLFile(GameDescriptor obj,String fileName){
        try {
            File file = new File(fileName);
            JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(obj, file);

        } catch (JAXBException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void objToFile(GameEngine obj, String fileName){
        try (ObjectOutputStream out =
                     new ObjectOutputStream(
                             new FileOutputStream(fileName+".csf"))) {
            out.writeObject(obj);
            out.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public static GameEngine fileToObj(String fileName){
        GameEngine obj = new GameEngine();
        try (ObjectInputStream in =
                     new ObjectInputStream(
                             new FileInputStream(fileName))) {
            obj = (GameEngine)in.readObject();
        }
        catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException");
        }

        return obj;
    }
}
