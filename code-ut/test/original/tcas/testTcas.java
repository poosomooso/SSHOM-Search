package original.tcas;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class testTcas {

	@Test
	public void test1(){
		int a1=958,a2=1,a3=1,a4=2597,a5=574,a6=4253,a7=0,a8=399,a9=400,a10=0,a11=0,a12=1;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
					a6,a7,a8,a9,a10,a11,a12);
		assertEquals(1, res);
	    
	    /* Cur_Vertical_Sep = Integer.parseInt(argv[1]+"");
	    if(Integer.parseInt(argv[2]+"")==0){
	    	High_Confidence=false;
	    }
	    else{
	    	High_Confidence=true;
	    }
	    //High_Confidence = Integer.parseInt(argv[2]+"");
	    if(Integer.parseInt(argv[3]+"")==0){
	    	Two_of_Three_Reports_Valid=false;
	    }
	    else{
	    	Two_of_Three_Reports_Valid=true;
	    }
	    //Two_of_Three_Reports_Valid = Integer.parseInt(argv[3]+"");
	    Own_Tracked_Alt = Integer.parseInt(argv[4]+"");
	    Own_Tracked_Alt_Rate = Integer.parseInt(argv[5]+"");
	    Other_Tracked_Alt = Integer.parseInt(argv[6]+"");
	    Alt_Layer_Value = Integer.parseInt(argv[7]+"");
	    Up_Separation =Integer.parseInt(argv[8]+"");
	    Down_Separation = Integer.parseInt(argv[9]+"");
	    Other_RAC = Integer.parseInt(argv[10]+"");
	    Other_Capability = Integer.parseInt(argv[11]+"");
	    Climb_Inhibit = Integer.parseInt(argv[12]+"");

	    System.out.println(alt_sep_test());
	    return;*/
	}
	@Test
	public void test2(){
		int a1=627,a2=0,a3=0,a4=621,a5=216,a6=382,a7=1,a8=400,a9=641,a10=1,a11=1,a12=0 ;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
				a6,a7,a8,a9,a10,a11,a12);
		assertEquals(0, res);
	}

	@Test
	public void test3(){
		int a1=549,a2=1,a3=1,a4=4398,a5=133,a6=1445,a7=1,a8=641,a9=639,a10=0,a11=0,a12=1;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
				a6,a7,a8,a9,a10,a11,a12);
		assertEquals(0, res);
	}

	@Test
	public void test4(){
		int a1=576,a2=0,a3=1,a4=3469,a5=183,a6=381,a7=2,a8=641,a9=501,a10=1,a11=0,a12=1;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
				a6,a7,a8,a9,a10,a11,a12);
		assertEquals(0, res);
	}

	@Test
	public void test5(){
		int a1=992,a2=1,a3=0,a4=3342,a5=23,a6=4657,a7=1,a8=640,a9=741,a10=0,a11=0,a12=0;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
				a6,a7,a8,a9,a10,a11,a12);
		assertEquals(0, res);
	}

	@Test
	public void test6(){
		int a1=548,a2=0,a3=1,a4=34,a5=542,a6=3514,a7=2,a8=499,a9=401,a10=1,a11=1,a12=1;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
				a6,a7,a8,a9,a10,a11,a12);
		assertEquals(0, res);
	}

	@Test
	public void test7(){
		int a1=710,a2=0,a3=0,a4=127,a5=403,a6=4616,a7=3,a8=500,a9=400,a10=0,a11=0,a12=0;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
				a6,a7,a8,a9,a10,a11,a12);
		assertEquals(0, res);
	}

	@Test
	public void test8(){
		int a1=638,a2=0,a3=1,a4=698,a5=499,a6=2465,a7=3,a8=500,a9=501,a10=0,a11=0,a12=0;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
				a6,a7,a8,a9,a10,a11,a12);
		assertEquals(0, res);
	}

	@Test
	public void test9(){
		int a1=893,a2=1,a3=0,a4=205,a5=283,a6=5056,a7=3,a8=400,a9=641,a10=1,a11=1,a12=1;
		Tcas newtcas=new Tcas();
		int res = newtcas.begin(13, a1, a2,a3,a4,a5,
				a6,a7,a8,a9,a10,a11,a12);
		assertEquals(0, res);
	}
}

