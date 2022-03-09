package tools.descartes.pmx.pcm.docker;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.Spring;

import javafx.application.Application;

//import com.sun.org.apache.xml.internal.security.Init;

//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.IO;

import sun.nio.ch.IOUtil;

import tools.descartes.pmx.pcm.docker.FileUploadController;
import tools.descartes.pmx.pcm.docker.StorageFileNotFoundException;
import tools.descartes.pmx.pcm.docker.StorageService;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.ui.Model;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.stream.Collectors;


@RestController
public class PcmProgramm {
	
	//default Value for Core arguments
	@Value("${cores:edge-uq38n=4, middletier-64bqq=4}")
	private String cores;
	
	//default value for DEBUG
	@Value("${debug:false}")
	private boolean debug;
	
	private String argumentCores = "--cores";
	private String argDash = "\"";
	
	//paths to result zip and output folder
	private static final String OUTPUT_ZIP_FILE = "/opt/zip/result.zip";
	private static final String SOURCE_FOLDER = "/opt/output";
	
	
	private static final String STATIC_ERROR_PATH = "src/main/resources/public/error";
	
	
	//getzip returns the results of last run
	@RequestMapping(
			value ="/getzip"
			)
	public void PmxGetResult(HttpServletResponse response)
	{
		File zipfile = new File("/opt/zip/result.zip");
		//final File zipfile = new File("/home/reed/results.zip");
		if ( zipfile.exists())
		{
			try
			{
				final InputStream inputStream = new FileInputStream(zipfile);
	            response.addHeader("Content-disposition", "attachment;filename=" + zipfile.getName());
	            response.setContentType("application/octet-stream");
	
	            IOUtils.copy(inputStream, response.getOutputStream());
			}
				catch(IOException e)
				{
					//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
				}
	        }
		else
		{
			try
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
			}
			catch(IOException e)
			{
				
			}
        }
	}
	
	
	//get Example file
	@RequestMapping(
			value ="/bookstore"
			)
	public void PmxExample2(HttpServletResponse response)
	{
		File zipfile = new File("/opt/examples/bookstore-KIEKER.zip.zip");
		//final File zipfile = new File("/home/reed/results.zip");
		if ( zipfile.exists())
		{
			try
			{
				final InputStream inputStream = new FileInputStream(zipfile);
	            response.addHeader("Content-disposition", "attachment;filename=" + zipfile.getName());
	            response.setContentType("application/octet-stream");
	
	            IOUtils.copy(inputStream, response.getOutputStream());
			}
				catch(IOException e)
				{
					//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
				}
	        }
		else
		{
			try
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
			}
			catch(IOException e)
			{
				
			}
        }
	}
	
	//get Example file
		@RequestMapping(
				value ="/calculator"
				)
		public void PmxExample1(HttpServletResponse response)
		{
			File zipfile = new File("/opt/examples/Calculator-KIEKER.zip");
			//final File zipfile = new File("/home/reed/results.zip");
			if ( zipfile.exists())
			{
				try
				{
					final InputStream inputStream = new FileInputStream(zipfile);
		            response.addHeader("Content-disposition", "attachment;filename=" + zipfile.getName());
		            response.setContentType("application/octet-stream");
		
		            IOUtils.copy(inputStream, response.getOutputStream());
				}
					catch(IOException e)
					{
						//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
					}
		        }
			else
			{
				try
				{
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
				}
				catch(IOException e)
				{
					
				}
	        }
		}

	//conf call /conf with arguments for URL to downalod zip file and additional argument for Cores
	@RequestMapping(
			value = "/conf"
			)
	public String PmxProgramm(HttpServletResponse response,
		@RequestParam(value="url", required=true) String url, 
		@RequestParam(value="core", required=false) String core)
	{
		System.out.println("URL:" + url);
		System.out.println("CoresArgument:" + cores);
		argumentCores = argDash+cores+argDash;
		runPmxProgramm(url, argumentCores, debug);

				File zipfile = new File("/opt/zip/result.zip");
		//final File zipfile = new File("/home/reed/results.zip");
		if ( zipfile.exists())
		{
			try
			{
				final InputStream inputStream = new FileInputStream(zipfile);
	            response.addHeader("Content-disposition", "attachment;filename=" + zipfile.getName());
	            response.setContentType("application/octet-stream");
	
	            IOUtils.copy(inputStream, response.getOutputStream());
			}
				catch(IOException e)
				{
					//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
				}
	        }
		else
		{
			try
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
			}
			catch(IOException e)
			{
				
			}
        	}
		return "success.html";
		
	}

	
	//main programm to run PMX
	private static void runPmxProgramm(String urlString, String arguments, boolean deb)
	{
		if (deb)
		{
			System.out.println("Running in debug-mode");
		}
		System.out.println("getting Zip-file from remote Server: ");
		System.out.println( urlString);
		
		//cleanin up old files
		deleteFolder(new File("/opt/download/"), false);
		deleteFolder(new File("/opt/input/"), false);
		deleteFolder(new File("/opt/output/"), false);
		
		//Download ZipFile From Server
		getZipFileFromServer(urlString);
		File downloadedFile = new File("/opt/download/Rawinput.zip");
		if (!downloadedFile.exists())
		{
			//return "downloadError.html";
		}
		unzipData("/opt/download/Rawinput.zip", "/opt/input/");

		//get the exact path to the Kiecker.MAP file
		String pathToKieckerMap = getKieckerFileDirectory("/opt/input/");
		if (deb)
		{
			System.out.println("Kiecker Map-file found at:");
			System.out.println(pathToKieckerMap);
			System.out.println(arguments);
		}
		System.out.println("starting pmx");
		//run PmxConsoleJAR
		try {
			String[] cmdArr = { "java", "-jar", "/opt/data/pmxConsole.jar", "-i",
					pathToKieckerMap + "/", "-o",
					"/opt/output/", "--cores", arguments };

			Process javaproc = Runtime.getRuntime().exec(cmdArr);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(javaproc.getInputStream()));
			while ((reader.readLine()) != null) {
				if (deb)
				{
					System.out.println(reader.readLine());
				}
			}
			try {
			javaproc.waitFor();
			} catch (InterruptedException e)
			{
				System.out.println("Error pmxConsole takes to Long");
				e.printStackTrace();
			}
		} catch (IOException e1) {
			System.out.println("Error Running pmxConsole cannot start");
			e1.printStackTrace();
		}
		//Zip results from PmxConsole
		System.out.println("Creating Zip File:");
		
		File directoryToZip = new File(SOURCE_FOLDER);
		List<File> fileList = new ArrayList<File>();
		try {
			System.out.println("---Getting references to all files in: " + directoryToZip.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.print("Error Reading files to zip");
			e.printStackTrace();
		}
		getAllFiles(directoryToZip, fileList, deb);
		writeZipFile(directoryToZip, fileList, deb);
		
		System.out.println("Done Zipping Results");
	}
	
	//downlods zip file from Server and inputs in Download folder
	private static void getZipFileFromServer(String urlstring) {
		try {
			URL url = new URL(urlstring);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			InputStream in = connection.getInputStream();
			FileOutputStream out = new FileOutputStream("/opt/download/Rawinput.zip");
			downloadFile(in, out, 1024);
			out.close();
		} catch (IOException e) {

		}
	}

	//downlaod File
	private static void downloadFile(InputStream in, OutputStream out, int buffer) {
		try {
			byte[] buf = new byte[buffer];
			int n = in.read(buf);
			while (n >= 0) {
				out.write(buf, 0, n);
				n = in.read(buf);
			}
			out.flush();
		} catch (IOException e) {

		}
	}

	//Unzip downloaded zip file
	private static void unzipData(String zipFile, String outputFolder) {
		byte[] buffer = new byte[1024];
		try {
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry zEntry = zis.getNextEntry();
			while (zEntry != null) {
				String fileName = zEntry.getName();
				File newFile = new File(outputFolder + File.separator + fileName);
				if (fileName.substring(fileName.length() - 1).equals("/")) {
					(new File(newFile.getAbsolutePath())).mkdirs();
				}
				else {
					new File(newFile.getParent()).mkdirs();
					FileOutputStream out = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					out.close();
				}
				zEntry = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//find the exact location of the Kiecker .MAP file downlaoded
	public static String getKieckerFileDirectory(String directoryToCheck) {
		File[] files = new File(directoryToCheck).listFiles();
		for (File file : files) {
			if (file.getName().contains(".map")) {
				return file.getParent();
			} else {
				if (file.isDirectory()) {
					String res = getKieckerFileDirectory(file.getAbsolutePath());
					if (res == null) {
						// do nothing
					} else
						return res;

				}
			}
		}

		return null;
	}
	
	public static void getAllFiles(File dir, List<File> fileList, boolean debugmode) {
		try {
			File[] files = dir.listFiles();
			for (File file : files) {
				fileList.add(file);
				if (file.isDirectory()) {
					if (debugmode)
					{
						System.out.println("directory:" + file.getCanonicalPath());
					}
					getAllFiles(file, fileList, debugmode);
				} else {
					if (debugmode)
					{
						System.out.println("     file:" + file.getCanonicalPath());
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//create zip and write results into it
	public static void writeZipFile(File directoryToZip, List<File> fileList, boolean debugmode) {

		try {
			FileOutputStream fos = new FileOutputStream(OUTPUT_ZIP_FILE);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (File file : fileList) {
				if (!file.isDirectory()) { // we only zip files, not directories
					addToZip(directoryToZip, file, zos, debugmode);
				}
			}

			zos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public static void addToZip(File directoryToZip, File file, ZipOutputStream zos, boolean debugmode)
			throws FileNotFoundException, IOException {

		FileInputStream fis = new FileInputStream(file);

		// we want the zipEntry's path to be a relative path that is relative
		// to the directory being zipped, so chop off the rest of the path
		String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
				file.getCanonicalPath().length());
		if (debugmode)
		{
			System.out.println("Writing '" + zipFilePath + "' to zip file");
		}
		ZipEntry zipEntry = new ZipEntry(zipFilePath);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}
	
	//delete Folders at Startup so programm starts fresch
	public static void deleteFolder(File folder, boolean deletefolder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f, true);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    if (deletefolder)
	    	folder.delete();
	}
	
	//Run the pcm programm
	@RequestMapping(
			value = "/runpcm"
			)
	public String Pcm(HttpServletResponse response, @RequestParam(value="id", defaultValue="999") String id)
	{
		
		System.out.println("Running with arguments: ");
		
		argumentCores = argDash+cores+argDash;
		
		System.out.println(argumentCores);
		if (debug)
		{
			System.out.println("Running in debug-mode");
		}
		//System.out.println("getting Zip-file from remote Server: ");
		//System.out.println( id);
		
		//cleanin up old files
		//deleteFolder(new File("/opt/download/"), false);
		deleteFolder(new File("/opt/input/"), false);
		deleteFolder(new File("/opt/output/"), false);
		deleteFolder(new File("/opt/zip/"), false);
		
		//Download ZipFile From Server
		//getZipFileFromServer(id);
		File downloadedFile = new File("/opt/download/Rawinput.zip");
		if (!downloadedFile.exists())
		{
			return "downloadError.html";
		}
		
		//unzip Data into folder
		//File test;
		
		//FileUtils.cleanDirectory("/opt/input/");
		//FileUtils.cleanDirectory("/opt/output/");
		unzipData("/opt/download/Rawinput.zip", "/opt/input/");

		//get the exact path to the Kiecker.MAP file
		String pathToKieckerMap = getKieckerFileDirectory("/opt/input/");
		if (debug)
		{
			System.out.println("Kiecker Map-file found at:");
			System.out.println(pathToKieckerMap);
			System.out.println(argumentCores);
		}
		System.out.println("starting pcm");
		//run PmxConsoleJAR
		try {
			String[] cmdArr = { "java", "-jar", "/opt/data/pcmConsole.jar", "-i",
					pathToKieckerMap + "/", "-o",
					"/opt/output/", "--cores", argumentCores };

			//System.out.println(cmdArr.toString());
			
			Process javaproc = Runtime.getRuntime().exec(cmdArr);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(javaproc.getInputStream()));
			while ((reader.readLine()) != null) {
				if (debug)
				{
					System.out.println(reader.readLine());
				}
			}
			try {
			javaproc.waitFor();
			} catch (InterruptedException e)
			{
				System.out.println("Error pmxConsole takes to Long");
				e.printStackTrace();
			}
			
			//try {
			//	javaproc.waitFor(90, TimeUnit.SECONDS);
			//} catch (InterruptedException e) {
			//	System.out.println("Error pmxConsole takes to Long");
			//	e.printStackTrace();
			//}
			//sleep(30*1000);
		} catch (IOException e1) {
			System.out.println("Error Running pcmConsole cannot start");
			e1.printStackTrace();
		}
		//Zip results from PmxConsole
		System.out.println("Creating Zip File:");
		
		File directoryToZip = new File(SOURCE_FOLDER);
		List<File> fileList = new ArrayList<File>();
		try {
			System.out.println("---Getting references to all files in: " + directoryToZip.getCanonicalPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.print("Error Reading files to zip");
			e.printStackTrace();
		}
		getAllFiles(directoryToZip, fileList, debug);
		writeZipFile(directoryToZip, fileList, debug);
		
		System.out.println("Done Zipping Results");
		
		//createZip("/opt/output", "/opt/zip/result");
		//return results as ZipFile
		
		File zipfile = new File("/opt/zip/result.zip");
		//final File zipfile = new File("/home/reed/results.zip");
		if ( zipfile.exists())
		{
			try
			{
				final InputStream inputStream = new FileInputStream(zipfile);
	            response.addHeader("Content-disposition", "attachment;filename=" + zipfile.getName());
	            response.setContentType("application/octet-stream");
	
	            IOUtils.copy(inputStream, response.getOutputStream());
			}
				catch(IOException e)
				{
					//response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
				}
	        }
		else
		{
			try
			{
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ZIP error");
			}
			catch(IOException e)
			{
				System.out.println("Error Providing ZipFile");
			}
        }
		return "success.html";
	}
	

}

