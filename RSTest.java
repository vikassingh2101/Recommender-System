import java.io.*;
import java.util.*;
class RSTest
{
int n,m;				// Dimensions of User-Item matrix
int num;				// Number of ratings in User-Item Matrix
int max_rating=5;
int rating[][];			// Containing User-Item matrix
int b[][];				// Marked matrix
int bT[][];				// Transpose of Marked Matrix
int bbT[][];				// Marked matrix and its transpose multiplied matrix
float a[][];				// Similarity Matrix
float bbTa[][];			// Association Matrix
float r_matrix3[][];			// Recommendation Matrix where path length=3
float r_matrix5[][];			// Recommendation Matrix where path length=5
	public void read()throws IOException						// Reading Training Set
	{
		FileReader fr=new FileReader("LargeDataset.txt");
		BufferedReader br=new BufferedReader(fr);
		String str=br.readLine();
		StringTokenizer st=new StringTokenizer(str,"	");
		n=Integer.parseInt(st.nextToken());
		m=Integer.parseInt(st.nextToken());
		rating=new int[n][m];
		b=new int[n][m];
		bT=new int[m][n];
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
			b[i-1][j-1]=bT[j-1][i-1]=1;
			n++;
		}
		fr.close();
	}							// Calculating bbT matrix
	public void bbT()
	{
		for(int i=0; i<n ; i++)
		{
			for(int j=0; j<n ; j++)
			{
				for(int k=0 ; k<m ; k++)
					bbT[i][j] += b[i][k]*bT[k][j];
			}
		}
	}
	public void a()						// Calculating Similarity Matrix
	{
		for( int i=0 ; i<n ; i++)
		{
			for(int j=0 ; j<n ; j++)
			{
				float sum=0;
				for(int k=0 ; k < m ; k++ )
				{
					if( b[i][k] == 1 && b[j][k] == 1 )
							sum+= ( max_rating - Math.abs( rating[i][k] - rating[j][k] ) ) / (float)max_rating ;
				}
				if( i == j )
					a[i][j]=1;
				else
					a[i][j]= sum/m;
			}
		}
					
	}
	public void bbTa()						// Calculating Association Matrix
	{
		for( int i=0 ; i<n ; i++)
		{
			for(int j=0 ; j<n ; j++)
				bbTa[i][j]=bbT[i][j]*a[i][j];
		}
	}
	public void r_Matrix3()					// Calculating Recommendation Matrix where path length=3
	{
		for(int i=0 ; i<n ;i++)
		{
			for(int j=0 ; j<m ; j++)
			{
				for(int k=0 ; k<n ;k++)
					r_matrix3[i][j] += bbTa[i][k]*rating[k][j];
			}
		}
	}
	public void r_Matrix5()throws IOException				// Calculating Recommendation Matrix where path length=5
	{
		for(int i=0 ; i<n ;i++)
		{
			for(int j=0 ; j<m ; j++)
			{
				for(int k=0 ; k<n ;k++)
					r_matrix5[i][j] += bbTa[i][k]*r_matrix3[k][j];
			}
		}	
	}
	public static void main(String ar[])throws Exception
	{
		
		RSTest obj=new RSTest();
		long lStartTime = new Date().getTime();
		
		obj.read();
		
		long lEndTime = new Date().getTime();
		long difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for generating Rating Matrix ( read() function ): " + difference/1000.0);
		
		lStartTime = new Date().getTime();
		
		obj.bbT();
		
		lEndTime = new Date().getTime();
		difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for computing bbT matrix ( bbT() function ): " + difference/1000.0);
		
		lStartTime = new Date().getTime();		

		obj.a();
		
		lEndTime = new Date().getTime();
		difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for computing Similarity Matrix ( a() function ): " + difference/1000.0);
		
		lStartTime = new Date().getTime();
		
		obj.bbTa();
		
		lEndTime = new Date().getTime();
		difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for computing bbTa matrix ( bbTa() function ): " + difference/1000.0);

		lStartTime = new Date().getTime();
		
		obj.r_Matrix3();


		lEndTime = new Date().getTime();
		difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for computing r_matrix3: " + difference/1000.0);

		lStartTime = new Date().getTime();

		
		obj.r_Matrix5();
		
		lEndTime = new Date().getTime();
		difference = lEndTime - lStartTime;
		System.out.println("Elapsed seconds for computing r_matrix5: " + difference/1000.0);
	}
}