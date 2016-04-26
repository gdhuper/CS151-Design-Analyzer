/**
 * @author Gurpreet Singh
 * Design Analyzer
 * HW5
 */
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DA {
	
	 List<Class> classList = new ArrayList<Class>(); 
		 public void loadPackage(String path) throws IOException, ClassNotFoundException {
		
	        System.out.println("Gathering class files in " + path);
	        FilenameFilter classFilter = new FilenameFilter() {
	            public boolean accept(File dir, String name) {
	                return name.toLowerCase().endsWith(".class");
	            }
	        };
	        File[] files = new File(path).listFiles(classFilter);

	        for(int i = 0; i < files.length; i++)
	        {
	        	String fullPath = files[i].getParent(); //full path of the target package 
	        	String dirName = fullPath.substring(fullPath.lastIndexOf("\\") + 1, fullPath.length());
	        	String name = files[i].getName();  // Class name A.class, B.class,...
	        	int indexDot = name.indexOf(".");
	        	String className = name.substring(0, indexDot); //A, B, C...
	        	String fQCName = dirName + "." + className;  //Fully qualified class name 
	        	Class c = Class.forName(fQCName);
	        	classList.add(c);
	        	
	        	
	        }
	     
		 }
		 /**
		  * Helper method to calculate total number of providers for a class
		  * @param c the class to calculate number of providers for
		  * @return total providers of class c
		  */
	public double calProviders(Class c) { 
		double p = 0;

		Field[] classfields = c.getDeclaredFields(); //gets the total fields in a class
		Method[] meths = c.getDeclaredMethods(); //gets all the methods declared in a class
		if (!c.getSuperclass().getName().equals("java.lang.Object")) {
			p++;
		}


		for (Field f : classfields) {
			for (Class l : classList) {
				if (f.getType().getName().equals(l.getName())) {
					p++;
				}
			}
		}
		//Checking the methods' return type and parameters against all classes
		for (Class l : classList) { 
			for (Method m : meths) {
				if (m.getReturnType().equals(l.getName())) {
					p++;
				}
				Class[] parameters = m.getParameterTypes();
				for (Class pa : parameters) {
					if (l.getName().equals(pa.getName())) {
						p++;
					}
				}
			}
		}
		
		return p;
	}
	     /**
	      * Helper method to calculates the total number of clients of a class 
	      * @param c the class to calculate total number of clients for
	      * @return total number of clients as a double value
	      */
	public double calClients(Class c) {
		double clients = 0;

		//Checking fields of each class in package against fields of the given class
		for(Class l : classList)
		{
			Field[] classFields = l.getDeclaredFields();
			for(Field f: classFields)
			{
				if(f.getType().getName().equals(c.getName()))
				{
					clients++;
				}
			}
		}
		
		//Checking methods of each class against methods of the given class
		for (Class l : classList) {
			Method[] meths = l.getDeclaredMethods();
			for (Method m : meths) {
				if (m.getReturnType().equals(c.getName())) {
					clients++;
				}
				Class[] parameters = m.getParameterTypes();
				for (Class pa : parameters) {
						if (pa.getName().equals(c.getName())) {
						clients++;
					}
				}
			}
		}
		//Checking if the given class is a super class of any class in the given package
		for (Class l : classList) {
			if (l.getSuperclass().getName().equals(c.getName())) 
			{
				clients++;
			}
		}

		return clients;

	}


	
	/**
	 * Calculates the depth of class
	 * @param c the class to calculate depth for 
	 * @return the depth of class as an integer
	 */
	private int inDepth(Class c)
	{ 
	 int depth = 1;
	 Class superClass = c.getSuperclass();
	 while(!superClass.getName().equals("java.lang.Object")) //Scans the depth of class until it hits the object class
	 	{
		 depth++; 
		 superClass = superClass.getSuperclass(); // to increment the while condition
	 	}
		return depth;
	}
	/**
	 * Calculates the responsibility of a class responsibility(A) = #clients/ #P
	 * @param c the class to calculate responsibility for 
	 * @return responsibility of class c in double
	 */
	private double responsibility(Class c)
	{
		double r = calClients(c);  //call to helper method to get total number of clients of c
	    double result = r/classList.size();
	    
	    //To cutoff result to two decimal places
	    BigDecimal d = new BigDecimal(result);
	    BigDecimal finalVal = d.setScale(2, RoundingMode.DOWN);
	    result = finalVal.doubleValue();
		return result;
	}
	
	/**
	 * Calculated  instability(A) = #providers(A)/#P
	 * @param c Class c 
	 * @return instability
	 */
	private double instability(Class c)
	{
		double i = calProviders(c);
	    double result = i/classList.size();
		//To cut off result to two decimal places	
	    BigDecimal d = new BigDecimal(result);
	    BigDecimal finalVal = d.setScale(2, RoundingMode.DOWN);
	    result = finalVal.doubleValue();
		return result; 
	}
	
	/**
	 * Calculates the workLoad of a class; workLoad = #methods in class/ #total methods in package.
	 * @param c the class to calculate workLoad for
	 * @return the workLoad of a class as a double value
	 */
	private double workLoad(Class c)
	{
		double total = c.getDeclaredMethods().length;
		double totalClassMethods = 0.0;
		
		for(Class l : classList)
		{
			double temp = l.getDeclaredMethods().length;
			totalClassMethods += temp;
		}
		double workLoad = total / totalClassMethods;
		
		//Cutting off result to two decimal places
		BigDecimal d = new BigDecimal(workLoad);
	    BigDecimal finalVal = d.setScale(2, RoundingMode.DOWN);
	    workLoad = finalVal.doubleValue();
		return workLoad; 
		
	}
	/**
	 * Method to display the metrics of the classes in a package 
	 */
	public void displayMetrics()
	{
		System.out.printf("%-5s%-20s%-20s%-20s%-20s%n", "C", "inDepth(C)", "instability(C)", "responsibility(C)", "workload(C)");
	
		for(Class c : classList)
		{
			double i = instability(c);
			double w = workLoad(c);
			int d = inDepth(c);
			double r = responsibility(c);
			System.out.printf("%-5s%5d%25.2f%20.2f%15.2f%n", c.getName().substring(c.getName().indexOf(".") + 1),d, i, r,  w);
		}
	}
	
	/**
	 * Main method to run the program
	 * @param args the  path to the package containing classes 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
        DA da = new DA();
        if (args.length != 1) {
            System.out.println("java DA <path>");
        } else {
            da.loadPackage(args[0]);
            da.displayMetrics();
        }
    }
	
	
}
	
