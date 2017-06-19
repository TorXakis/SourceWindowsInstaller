package nl.tno.torxakis.wxsgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WxsGeneratorMain {
	final static String WXS_FILE_NAME = "TorXakis.wxs";

	public static void main(String[] args) {
		if ( args.length != 5) {
			System.out.println("Usage: WxsGenerator <tag_name> <torxakis sandbox name> <z3 folder name> <CVC4 folder name> <torxakis version number>");
		}
		else {
			String svnTag = args[0];
			String torxakisSandbox = args[1];
			String z3FolderName = args[2];
			String cvc4FolderName = args[3];
			String versionNumber = args[4];
			
			final String branchFolderName        = torxakisSandbox + "\\" + svnTag;
			final String torxakisFolderName      = branchFolderName + "\\torxakis";
			final String examplesFolderName      = torxakisFolderName + "\\examps";
			final String eclipsePluginFolderName = branchFolderName + "\\EclipsePlugin\\nl.tno.torxakis.language.update-site";
			final String editorPluginsFolderName = torxakisSandbox + "\\" + svnTag + "\\WindowsInstaller\\EditorPlugins";

			prepareEditorPluginsFolder( eclipsePluginFolderName, branchFolderName );	

			TDirectory exampsContent = getDirectory( examplesFolderName );
			TDirectory z3Content= getDirectory( z3FolderName );
			TDirectory cvc4Content = getDirectory( cvc4FolderName );
			TDirectory editorPluginsContent = getDirectory( editorPluginsFolderName );

			generateLicenseRtf( branchFolderName );
			
			WxsGenerator generator = new WxsGenerator( svnTag, z3Content, cvc4Content, exampsContent, editorPluginsContent, versionNumber, branchFolderName );
			
			String result = generator.generate();
			
			// Write result to file
			try {
				PrintWriter out = new PrintWriter( branchFolderName + "\\WindowsInstaller\\" + WXS_FILE_NAME);
				out.print( result );
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println( "Finished generating installer wxs file" );
		}
	}
	
	
	
	private static void generateLicenseRtf(String branchFolderName) {
		String txtFileName = branchFolderName + "\\torxakis\\license.txt";
		String rtfFileName = branchFolderName + "\\WindowsInstaller\\license.rtf";
		try {
			List<String> txtLines = Files.readAllLines( Paths.get( txtFileName) );
			
			FileWriter fw = new FileWriter( rtfFileName );
			
			// Write header
			fw.write( "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deflang1033{\\fonttbl{\\f0\\fnil\\fcharset0 Calibri;}}{\\colortbl ;\\red0\\green0\\blue255;}{\\*\\generator Msftedit 5.41.21.2512;}\\viewkind4\\uc1\\pard\\sl240\\slmult1\\lang9\\f0\\fs22 " );
			
			for ( String txtLine: txtLines ) {
				fw.write( txtLine + "\\par\n");
			}
			fw.write( "\0");
			
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static void prepareEditorPluginsFolder(String eclipsePluginFolderName, String branchFolderName) {
		final String editorPluginsFolderName = branchFolderName + "\\WindowsInstaller\\EditorPlugins";

		// Eclipse
		createZipFileFromDirectory( eclipsePluginFolderName, editorPluginsFolderName + "\\Eclipse" );
		
		// Notepad++
		copyNotepadPP( branchFolderName, editorPluginsFolderName + "\\Notepad++" );
	}




	private static void copyNotepadPP(String branchFolderName, String notepadFolder) {
    	InputStream inStream = null;
    	OutputStream outStream = null;
		
    	try{
    		String sourceFileName = branchFolderName + "\\Notepad++\\TorXakis.xml";
    		String destinationFileName = notepadFolder + "\\TorXakis.xml";
    		
    	    File source = new File( sourceFileName );
    	    File destination = new File( destinationFileName );
    		
    	    inStream = new FileInputStream(source);
    	    outStream = new FileOutputStream(destination);
        	
    	    byte[] buffer = new byte[1024];
    	    int length;

    	    //copy the file content in bytes 
    	    while ((length = inStream.read(buffer)) > 0) {
    	  
    	    	outStream.write(buffer, 0, length);
    	 
    	    }
    	 
    	    inStream.close();
    	    outStream.close();

    	}catch(IOException e){
    	    e.printStackTrace();
    	}
    }


	private static TDirectory getDirectory( String rootFolderName ) {
		TDirectory root = new TDirectory( rootFolderName );	
		visitAllDirsAndFiles( new File(rootFolderName), root );
		
		return root.getDirectories().get(0);
	}

	public static void visitAllDirsAndFiles(File dir, TDirectory directory) {
	   	if (dir.isDirectory()) {
//			System.out.println("Directory " + dir);
			String[] children = dir.list();
			TDirectory childDirectory = new TDirectory( dir.toString() );
			directory.getDirectories().add( childDirectory );
			for (int i = 0; i < children.length; i++) {
				visitAllDirsAndFiles(new File(dir, children[i]), childDirectory );
			}
	   	}
		else {
			if (dir.isFile()) {
				directory.getFiles().add( new TFile( dir.toString() ) );
//			    System.out.println("File " + dir);
			}
		}
	}
	
	public static void createZipFileFromDirectory( String srcDir, String eclipseEditorPluginsFolderName ) {
       String zipFile = eclipseEditorPluginsFolderName + "\\EclipsePlugin.zip";
                
       try {
           ZipOutputStream zos = new ZipOutputStream( new FileOutputStream(zipFile) );
           File dir = new File(srcDir);

           File[] files = dir.listFiles();
           for ( int i = 0; i < files.length; i++ ) {
               if ( files[i].isDirectory() ) {
            	   zipDirectory( srcDir, files[i], zos );
               }
               else {
            	   zipFile( srcDir, files[i], zos );
               }
           }

           // close the ZipOutputStream
           zos.close();
       }
       catch (IOException ioe) {
           System.out.println("Error creating zip file" + ioe);
       }
	
	}


	private static void zipFile(String root, File file, ZipOutputStream zos) throws IOException {
        int length;

        // create byte buffer
        byte[] buffer = new byte[1024];

        FileInputStream fis = new FileInputStream( file );

        // begin writing a new ZIP entry, positions the stream to the start of the entry data
        zos.putNextEntry( new ZipEntry( file.getAbsolutePath().replace( root + "\\", "") ) );

        while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }
        zos.closeEntry();
        fis.close();
	}


	private static void zipDirectory(String root, File srcDir, ZipOutputStream zos) throws IOException {
        // begin writing a new ZIP entry, positions the stream to the start of the entry data
        zos.putNextEntry( new ZipEntry( srcDir.getName() + "\\" ) );
        zos.closeEntry();
        
        try {
            File dir = new File(srcDir.getAbsolutePath());

            File[] files = dir.listFiles();
            for ( int i = 0; i < files.length; i++ ) {
                if ( files[i].isDirectory() ) {
             	   zipDirectory( root, files[i], zos );
                }
                else {
             	   zipFile( root, files[i], zos );
                }
            }
        }
        catch (IOException ioe) {
            System.out.println("Error creating zip file" + ioe);
        }
	}
}
