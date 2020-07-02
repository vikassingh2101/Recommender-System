import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.math.*;
class RecommenderSystem
{
int n,m;
int num;
int max_rating=5;
int rating[][];
int b[][];
int bbT[][];
float a[][];
float bbTa[][];
float r_matrix3[][];
float r_matrix5[][];
	public void read()throws IOException
	{
		FileReader fr=new FileReader("LargeDataset.txt");
		BufferedReader br=new BufferedReader(fr);
		String str=br.readLine();
		StringTokenizer st=new StringTokenizer(str,"	");
		n=Integer.parseInt(st.nextToken());
		m=Integer.parseInt(st.nextToken());
		rating=new int[n][m];
		b=new int[n][m];
		bbT=new int[n][n];
		a=new float[n][n];
		bbTa=new float[n][n];
		r_matrix3=new float[n][m];
		r_matrix5=new float[n][m];
		str=br.readLine();
		num=Integer.parseInt(str);
		int n=1;
		while( n <= num )
		{
			str=br.readLine();
			st=new StringTokenizer(str,"	");
			int i=Integer.parseInt(st.nextToken());
			int j=Integer.parseInt(st.nextToken());
			int k=Integer.parseInt(st.nextToken());
			rating[i-1][j-1]=k;
			b[i-1][j-1]=1;
			n++;
		}
		fr.close();
	}
	public static void main(String ar[])throws Exception
	{
	
		RecommenderSystem obj=new RecommenderSystem();

		//long lStartTime = new Date().getTime();

		obj.read();

		/*long lEndTime = new Date().getTime();
		long difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for generating Rating Matrix ( read() function ): " + difference/1000.0);

		lStartTime = new Date().getTime();*/	
		
		CountDownLatch cdl=new CountDownLatch(1);
		new BBTA(obj,cdl);
		cdl.await();

		
		/*lEndTime = new Date().getTime();
		difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for computing bbTa matrix ( bbTa() function ): " + difference/1000.0);

		lStartTime = new Date().getTime();*/


		cdl=new CountDownLatch(obj.m);
		
		for(int i=0 ; i < obj.m ; i++)
			new R_Matrix3Thread(obj,i,cdl);

		cdl.await();

		int count=0;
		for( int i=0 ; i<obj.n ; i++)
		{
			for(int j=0 ; j<obj.m ; j++)
				if( obj.r_matrix3[i][j] == 0 )
					count++;
		}
		System.out.println("Number of pairs in R_Matrix3 which are still zero= "+count);

		System.out.println("Number of pairs in R_Matrix5 which are still zero= 0");


		/*lEndTime = new Date().getTime();
		difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for computing r_matrix3: " + difference/1000.0);

		lStartTime = new Date().getTime();
	
		cdl=new CountDownLatch(obj.m);
		for(int i=0 ; i < obj.m ; i++)
			new R_Matrix5Thread(obj,i,cdl);
		cdl.await();
		
		lEndTime = new Date().getTime();
		difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for computing r_matrix5: " + difference/1000.0);*/
	}
}
class BBTA implements Runnable
{
RecommenderSystem obj;
CountDownLatch cdl;
	public BBTA(RecommenderSystem obj,CountDownLatch cdl)
	{
		this.obj=obj;
		this.cdl=cdl;
		new Thread(this).start();
	}
	public void run()
	{
		CountDownLatch cdl1=new CountDownLatch(obj.n);
		for( int i=0; i < obj.n ;i++)
		{
			new BBTAThread(obj,i,cdl1);
		}
		try
		{
			cdl1.await();
		}
		catch(InterruptedException e)
		{}
		cdl.countDown();
	}
}
class BBTAThread implements Runnable
{
RecommenderSystem obj;
int i;
CountDownLatch cdl1;
	public BBTAThread(RecommenderSystem obj,int i,CountDownLatch cdl1)
	{
		this.obj=obj;
		this.i=i;
		this.cdl1=cdl1;
		Thread t=new Thread(this);
		t.start();
	}
	public void run()
	{
		for(int j=0 ; j <= i ; j++ )
		{
			obj.bbT[i][j]=0;
			float sum=0;
			for(int k=0 ; k < obj.m ; k++ )
			{
				obj.bbT[i][j] +=obj.b[i][k]*obj.b[j][k];
				if( obj.b[i][k] == 1 && obj.b[j][k] == 1 )
						sum+= ( obj.max_rating - Math.abs( obj.rating[i][k] - obj.rating[j][k] ) ) / (float)obj.max_rating ;
			}
			if( i == j )
				obj.a[i][j]=1;
			else
				obj.a[i][j]= sum/obj.m;
			obj.bbTa[i][j]=obj.bbTa[j][i]=obj.bbT[i][j] * obj.a[i][j];
		}
		cdl1.countDown();
	}	
}
class R_Matrix3Thread implements Runnable
{
RecommenderSystem obj;
int i;
CountDownLatch cdl;
	public R_Matrix3Thread(RecommenderSystem obj,int i,CountDownLatch cdl)
	{
		this.obj=obj;
		this.i=i;
		this.cdl=cdl;
		new Thread(this).start();
	}
	public void run()
	{
		for(int j=0 ; j < obj.n ; j++ )
		{
			obj.r_matrix3[j][i]=0;
			for(int k=0 ; k < obj.n ; k++ )
				obj.r_matrix3[j][i] +=obj.bbTa[j][k] * obj.rating[k][i];
		}
		cdl.countDown();
	}
}
class R_Matrix5Thread implements Runnable
{
RecommenderSystem obj;
int i;
CountDownLatch cdl;
	public R_Matrix5Thread(RecommenderSystem obj,int i,CountDownLatch cdl)
	{
		this.obj=obj;
		this.i=i;
		this.cdl=cdl;
		new Thread(this).start();
	}
	public void run()
	{
		for(int j=0 ; j < obj.n ; j++ )
		{
			obj.r_matrix5[j][i]=0;
			for(int k=0 ; k < obj.n ; k++ )
				obj.r_matrix5[j][i] +=obj.bbTa[j][k] * obj.r_matrix3[k][i];
		}
		cdl.countDown();
	}
}