package br.com.gooverapp.importshow;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientException;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoServerException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Filters.eq; 

public class GooverImport {
	
	static final Logger logger = LogManager.getLogger(GooverImport.class.getName());
    private static Client client;
    private static WebTarget target;
    private static final String apiKey = "2a0b9b15badfe1446d5678c8165592c5";
    public static String[] idVeiculos;
    public static String[] idCategorias;
    public static String mongourl = "";// = "mongodb://localhost:27017";
    public static Hashtable<Integer, String> generoSource = new Hashtable<Integer,String>();
    public static HashMap<Integer, String>  generoMap = new HashMap(generoSource);
    public static MongoClient clientMongo = null;
    public static MongoDatabase database = null;
    private final CodecRegistry codecRegistry = null;
    private static ArrayList<String> listNotExistImagem = new ArrayList();
    private static ArrayList<String> listNotExistSinopse = new ArrayList();
    private static ArrayList<String> listNotExistGenero = new ArrayList();
    private static ArrayList<String> listNotExistPrograma = new ArrayList();
    
    /**
	 * @param args
	 * @throws FileNotFoundException 
	 * Os argumentos recebidos são o seguinte:
	 * args[0] - id do Veículo
	 * args[1] - id da Categoria
	 * 
	 */
	public static void main(String[] args) throws FileNotFoundException {

		logger.info("-------------- INÍCIO DA IMPORTAÇÃO DE PROGRAMAS --------------");
	        
	    //setar os ids de Genero
	    setGenero();
	    setProperties();
	    
	    try{
	    	//conecta no MongoDB
	    	System.out.println("CONECTANDO COM O BANCO DE DADOS");
	    	clientMongo = new MongoClient(new MongoClientURI(mongourl));
		    database = clientMongo.getDatabase("gooverdb");
	    }catch(MongoClientException me){
			System.out.println("ERRO DE CONEXÃO COM O BANCO DE DADOS");
			logger.info("ERRO DE CONEXÃO COM O BANCO DE DADOS");
		}finally{
			System.out.println("BANCO DE DADOS CONECTADO COM SUCESSO");
		    try{
		    	logger.info("-------------- INÍCIO DA LEITURA DO ARQUIVO listimport.txt --------------");
		    	System.out.println("-------------- INÍCIO DA LEITURA DO ARQUIVO listimport.txt --------------");
			    Scanner scanner = new Scanner(new FileReader("./file/listimport.txt"));
			    String nomePrograma = "";
				while (scanner.hasNext()) {
					nomePrograma = scanner.nextLine();
					logger.info("IMPORTANDO PROGRAMA: " + nomePrograma);
					System.out.println(">> Importando - " + nomePrograma);
					if(!existPrograma(nomePrograma)){
						getRequest(nomePrograma);
					}					
				}
				System.out.println("-------------- FIM DA IMPORTAÇÃO DE PROGRAMAS --------------");
				logger.info("-------------- FIM DA IMPORTAÇÃO DE PROGRAMAS --------------");
				
				//Imprime Estatisticas
				String strStats = getStats();
				System.out.println(strStats);
				logger.info(strStats);
				
			    scanner.close();
			} catch (FileNotFoundException fe) {
				logger.error("ERRO NA LEITURA DO ARQUIVO listimport.txt ", fe);
			}
		}
	}
	
	
    public static void getRequest(String nomePrograma){
        client = ClientBuilder.newClient();
        //exemplo http://api.themoviedb.org/3/search/tv?api_key=2a0b9b15badfe1446d5678c8165592c5&language=pt&query=Orange%20Is%20the%20New%20Black
        //http://api.themoviedb.org/3/genre/tv/list?api_key=2a0b9b15badfe1446d5678c8165592c5
        target = client.target(
                "http://api.themoviedb.org/3/search/tv")
                .queryParam("api_key", apiKey)
                .queryParam("language", "pt")
                ;

        String retornoPrograma = target.queryParam("query", nomePrograma)
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
		
        JSONObject obj = new JSONObject(retornoPrograma);
        JSONArray programas = obj.getJSONArray("results");
        if(programas != null){
        	if(programas.length() > 0){
	        	JSONObject programa = (JSONObject)programas.get(0);
	        	//System.out.println(getProgramaJSON(programa));
	        	//Inserir o programa no banco de dados
	        	insertPrograma(getProgramaJSON(programa));
        	}else{
        		listNotExistPrograma.add(nomePrograma);
        		//logger.warn("---PROGRAMA NÃO ENCONTRADO - " + nomePrograma);
    		   // System.out.print(" Programa não encontrado");
        	}
        }else{
        	listNotExistPrograma.add(nomePrograma);
		    //logger.warn("---PROGRAMA NÃO ENCONTRADO - " + nomePrograma);
		    //System.out.print(" Programa não encontrado");
        }
        
	}
    
    public static void insertPrograma(JSONObject programa){
    	try{
	    	MongoCollection<Document> collection = database.getCollection("programas");
	    	collection.insertOne(Document.parse(programa.toString()));
	    }catch(MongoWriteException me){
    		logger.error("----ERRO INSERINDO NO BANCO DE DADOS");
    	}
    }

    public static boolean existPrograma(String nomePrograma){
    	boolean exist = false;
    	try{
	    	MongoCollection<Document> collection = database.getCollection("programas");
	    	
	        BasicDBObject regexQuery = new BasicDBObject();
	        regexQuery.put("titulo",new BasicDBObject("$regex", nomePrograma).append("$options", "i"));
	        Document first = collection.find(regexQuery).first();
	 
	    	//inserir um documento
	    	if(first != null){
	    		logger.warn("---PROGRAMA JÁ EXISTE NA BASE DE DADOS - " + nomePrograma);
	    		System.out.print(" Programa já existe na base de dados - ");
	    		exist = true;	    		
	    	}
    	}catch(MongoWriteException me){
    		logger.error("----ERRO INSERINDO NO BANCO DE DADOS");
    	}
    	return exist;
    }

    
    
    public static JSONObject getProgramaJSON(JSONObject jobj){
    	JSONObject progObj = new JSONObject();

    	TimeZone tz = TimeZone.getTimeZone("UTC");
    	DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
    	df.setTimeZone(tz);
    	String nowAsISO = df.format(new Date());
            	
    	progObj.put("slug", jobj.getString("original_name").replaceAll("\\s", "_").toLowerCase());
    	progObj.put("titulo", jobj.getString("original_name"));
    	progObj.put("destaques", new String[0]);
    	progObj.put("status", "rascunho");
    	progObj.put("dataPublicacao", nowAsISO);
    	
    	progObj.put("conteudo", new JSONObject().put("sinopse", optString(jobj, "overview")));
    	progObj.put("lancamento", false);
    	progObj.put("__v", 1);
    	
    	if(optString(jobj, "overview").isEmpty()){
    		listNotExistSinopse.add(jobj.getString("original_name"));
    		//logger.warn("----SINOPSE NÃO ENCONTRADA - " + jobj.getString("original_name"));
    	}
    	
    	//Veiculo
    	if(idVeiculos != null){
    		JSONArray voarray = new JSONArray();
    		for(int i=0;i<idVeiculos.length;i++){
				JSONObject vo = new JSONObject();
				vo.put("$oid", idVeiculos[i]);
				voarray.put(vo);
    		}
    		progObj.put("veiculos", voarray);
    	}
   	
    	//Categoria
    	if(idCategorias != null){
    		JSONArray coarray = new JSONArray();
    		for(int i=0;i<idCategorias.length;i++){
				JSONObject co = new JSONObject();
				co.put("$oid", idCategorias[i]);
				coarray.put(co);
    		}
    		progObj.put("categorias", coarray);
    	}
    	
    	//Genero
    	JSONArray generoMDBArray = jobj.getJSONArray("genre_ids");
    	if(generoMDBArray.length() <= 0) {
    		listNotExistGenero.add(jobj.getString("original_name"));    	
    	}
       	JSONArray generoList = new JSONArray();
    	for(int i=0; i < generoMDBArray.length(); i++){
    		if(generoMap.get(generoMDBArray.get(i)) != null){
	    		JSONObject coMDB = new JSONObject();
	    		coMDB.put("$oid", generoMap.get(generoMDBArray.get(i)));
	    		generoList.put(coMDB);
    		}
    	}
    	
    	progObj.put("genero", generoList);

    	/**Download da Imagem**/
    	try {
    		String imagem = optString(jobj, "backdrop_path");
    		if(imagem == null){
    			imagem = optString(jobj, "poster_path");
    		}
    		
    		
    		if(imagem != null){
    		
	    		BufferedImage bImage = null;
	    		
	    	    URL url = new URL("https://image.tmdb.org/t/p/original"+imagem);
	    	    
	    	    bImage = ImageIO.read(url);
	            ImageIO.write(bImage, "jpg", new File("./img/imagetmp.jpg"));
	            
	            //Upload da Imagem para o Cloudinary
	            try{
	                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
	              		  "cloud_name", "goover",
	              		  "api_key", "798733831758167",
	              		  "api_secret", "OfwJGGaNyCiE-8RsdP_y0Kb827s"));
	              
	              Map uploadResult = cloudinary.uploader().upload(new File("./img/imagetmp.jpg"), ObjectUtils.emptyMap());
	              
	              JSONObject backdropObj = new JSONObject();
	              backdropObj.put("public_id", (String) uploadResult.get("public_id"));
	              backdropObj.put("version", ((Integer) uploadResult.get("version")).intValue());
	              backdropObj.put("signature", (String) uploadResult.get("signature"));
	              backdropObj.put("width", ((Integer) uploadResult.get("width")).intValue());
	              backdropObj.put("height", ((Integer) uploadResult.get("height")).intValue());
	              backdropObj.put("format", (String) uploadResult.get("format"));
	              backdropObj.put("resource_type", (String) uploadResult.get("resource_type"));
	              backdropObj.put("url", (String) uploadResult.get("url"));
	              backdropObj.put("secure_url", (String) uploadResult.get("secure_url"));
	              progObj.put("backdrop",backdropObj);
	              
	              logger.info("----UPLOAD POSTER "+jobj.getString("original_name")+"- [OK]");
	              
	            }catch(Exception cie){
	            	logger.warn("----UPLOAD POSTER "+jobj.getString("original_name")+"- [NOK]", cie);
	            }
    		}else{
    			listNotExistImagem.add(jobj.getString("original_name"));  
    		}
    	} catch (IOException e) {
    		listNotExistGenero.add(jobj.getString("original_name"));  
    		logger.error("ERRO AO FAZER DOWNLOAD DA IMAGEM - " + jobj.getString("original_name") );
    	}
    	
    	//https://image.tmdb.org/t/p/original/5lW1fcWpx89dibAFfdGNlcvkdV8.jpg
    	
    	return progObj;
    }
    
	/**
	 * Metodo DE-PARA dos ids do themoviedb 
	 * para o banco de dados do Goover
	 */
    public static void setGenero(){
    	generoMap.put(10759,"5768a5600b12fbe73f0c28ac");
    	generoMap.put(16,"5768a5600b12fbe73f0c28ae");
    	generoMap.put(35,"5768a5600b12fbe73f0c28be");
    	generoMap.put(80,"5768a5600b12fbe73f0c28bf");
    	generoMap.put(99,"5768a5600b12fbe73f0c28b1");
    	generoMap.put(18,"5768a5600b12fbe73f0c28b2");
    	generoMap.put(10751,"5768a5600b12fbe73f0c28b3");
    	generoMap.put(10762,"5768a5600b12fbe73f0c28b5");
    	generoMap.put(9648,"5768a5600b12fbe73f0c28c4");
    	generoMap.put(10763,"5768a5600b12fbe73f0c28b6");
    	generoMap.put(10764,"5768a5600b12fbe73f0c28b8");
    	generoMap.put(10765,"5768a5600b12fbe73f0c28b9");
    	generoMap.put(10766,"5768a5600b12fbe73f0c28ba");
    	generoMap.put(10767,"5768a5600b12fbe73f0c28bb");
    	generoMap.put(10768,"5768a5600b12fbe73f0c28bc");
    	generoMap.put(37,"5768a5600b12fbe73f0c28bd");
	}
    
    public static String optString(JSONObject json, String key)
    {
        if (json.isNull(key))
            return null;
        else
            return json.optString(key, null);
    }
    
    public static void setProperties(){
    	
    	Properties prop = new Properties();
    	InputStream input = null;
    	
    	try {
    		input = new FileInputStream("./conf/config.properties");
    		prop.load(input);
    		
		    idVeiculos = prop.getProperty("veiculos").split(",");
		    idCategorias = prop.getProperty("categorias").split(",");
    		mongourl = prop.getProperty("mongourl");

    	} catch (IOException ex) {
    		ex.printStackTrace();
    	} finally {
    		if (input != null) {
    			try {
    				input.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
    public static String getStats(){
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append("\n--------------------------------------------------------");
    	sb.append("\n------------------- Estatísticas -----------------------");
    	sb.append("\n--------------------------------------------------------");
    	sb.append("\n--- Programas sem Sinopse ------------------------------");
    	for(int i=0; i < listNotExistSinopse.size();i++){
    		sb.append("\n" + listNotExistSinopse.get(i));
    	}
    	sb.append("\n--- Programas sem Imagem -------------------------------");
    	for(int i=0; i < listNotExistImagem.size();i++){
    		sb.append("\n" + listNotExistImagem.get(i));
    	}
    	sb.append("\n--- Programas sem Genero -------------------------------");
    	for(int i=0; i < listNotExistGenero.size();i++){
    		sb.append("\n" + listNotExistGenero.get(i));
    	}
    	sb.append("\n--- Programas não encontrados --------------------------");
    	for(int i=0; i < listNotExistPrograma.size();i++){
    		sb.append("\n" + listNotExistPrograma.get(i));
    	}    	
		sb.append("\n--------------------------------------------------------");

		return sb.toString();
    }


}

/*

{
"_id": {
    "$oid": "578a391a734334ff15f77304"
},
"slug": "roda-viva",ok
"titulo": "Roda Viva",ok
"destaques": [],ok
"veiculos": [
    {
        "$oid": "5768a4b80b12fbe73f0c2832"
    }
],
"genero": [
    {
        "$oid": "5768a5600b12fbe73f0c28b6"
    },
    {
        "$oid": "578a39b7734334ff15f77305"
    }
],
"categorias": [
    {
        "$oid": "5761c1c20647b7b8b352fa88"
    }
],
"dataPublicacao": {
    "$date": "2016-07-16T04:00:00.000Z"
},
"status": "publicado",
"__v": 2,
"backdrop": {
    "public_id": "xbrlyoyyyd3uyz0ca22y",
    "version": 1468676491,
    "signature": "1851eb720c0d4fe1bc9c1293e42d4e3013e6ad4b",
    "width": 370,
    "height": 185,
    "format": "png",
    "resource_type": "image",
    "url": "http://res.cloudinary.com/goover/image/upload/v1468676491/xbrlyoyyyd3uyz0ca22y.png",
    "secure_url": "https://res.cloudinary.com/goover/image/upload/v1468676491/xbrlyoyyyd3uyz0ca22y.png"
},
"conteudo": {
    "sinopse": "Desde 1986, quando a democracia engatinhava após o regime militar, a emissora abriu um espaço plural para a apresentação de ideias, conceitos e análises sobre temas de interesse da população, num espaço raro na televisão para a reflexão não só da realidade brasileira e mundial, como do próprio jornalismo e dos jornalistas."
}
}

*/