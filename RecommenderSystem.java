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
		long lStartTime = new Date().getTime();
		RecommenderSystem obj=new RecommenderSystem();
		obj.read();
		CountDownLatch cdl=new CountDownLatch(2);
		new BBT(obj,cdl);
		new Similarity_MatrixThread(obj,cdl);
		cdl.await();
		for(int i=0 ; i<obj.n ; i++)
		{
			for(int j=0 ; j<=i ; j++)
				obj.bbTa[i][j]=obj.bbTa[j][i]=obj.bbT[i][j] * obj.a[i][j];
		}
		cdl=new CountDownLatch(obj.n);
		for(int i=0 ; i < obj.n ; i++)
			new R_Matrix3Thread(obj,i,cdl);
		cdl.await();
		/*for( int i=0 ; i<obj.n ; i++)
		{
			for(int j=0 ; j<obj.m ; j++)
				System.out.println( i+","+j+"	     "+obj.r_matrix5[i][j]);
		}*/
		long lEndTime = new Date().getTime();
		long difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds: " + difference/1000.0);
	}
}
class BBT implements Runnable
{
RecommenderSystem obj;
CountDownLatch cdl;
	public BBT(RecommenderSystem obj,CountDownLatch cdl)
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
			new BBTThread(obj,i,cdl1);
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
class BBTThread implements Runnable
{
RecommenderSystem obj;
int i;
CountDownLatch cdl1;
	public BBTThread(RecommenderSystem obj,int i,CountDownLatch cdl1)
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
			for(int k=0 ; k < obj.m ; k++ )
				obj.bbT[i][j] +=obj.b[i][k]*obj.b[j][k];
		}
		cdl1.countDown();
	}	
}
class Similarity_MatrixThread implements Runnable
{
RecommenderSystem obj;
CountDownLatch cdl;
	public Similarity_MatrixThread(RecommenderSystem obj,CountDownLatch cdl)
	{
		this.obj=obj;
		this.cdl=cdl;
		new Thread(this).start();
	}
	public void run()
	{
		for( int i=0 ; i<obj.n ;i++ )
		{
			for( int j=0 ; j < i ; j++)
			{
				float sum=0;
				for( int k=0 ; k < obj.m ; k++)
				{
					if( obj.b[i][k] == 1 && obj.b[j][k] == 1 )
						sum+= ( obj.max_rating - Math.abs( obj.rating[i][k] - obj.rating[j][k] ) ) / (float)obj.max_rating ;
				}
				obj.a[i][j]= sum/obj.m;
			}
		}
		for( int i=0 ; i < obj.n ; i++)
			obj.a[i][i]=1;
		cdl.countDown();
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
		for(int j=0 ; j < obj.m ; j++ )
		{
			obj.r_matrix3[i][j]=0;
			for(int k=0 ; k < obj.n ; k++ )
				obj.r_matrix3[i][j] +=obj.bbTa[i][k]*obj.rating[k][j];
			new R_Matrix5Thread(obj,i,j);
		}
		cdl.countDown();
	}
}
class R_Matrix5Thread implements Runnable
{
RecommenderSystem obj;
int i,j;
	public R_Matrix5Thread(RecommenderSystem obj,int i,int j)
	{
		this.obj=obj;
		this.i=i;
		this.j=j;
		new Thread(this).start();
	}
	public void run()
	{
		for(int k=0 ; k < obj.n ; k++ )
		{
			obj.r_matrix5[k][j] += obj.bbTa[k][i] * obj.r_matrix3[i][j];
		}
	}
}