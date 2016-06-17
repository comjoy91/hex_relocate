package sKorea_mapping;

import processing.core.*;
import processing.data.XML;

public class Consti_Map { // 처음 지역구 정보를 받는 곳이자, 전체 지역구/다각형의 좌표, 크기 등의 정보를 저장하는 곳. 마지막에는 이 곳에서 print_map()을 호출해서 지도 출력.
	
	public static Processing_sketch parent; // 부모 Processing_sketch
	public static float SQRT_3 = (float)Math.sqrt(3);

	private int grid_x_max; //최대 grid_X
	private int grid_x_min; //최소 grid_X
	private int grid_y_max; //최대 grid_y
	private int grid_y_min; //최소 grid_y
	public int bangwooi_num = 6; //총 방위개수.
	// (x-1 y-1):0 / (x-1 y):1 / (x y-1):3 / (x y+1):2 / (x+1 y):4 / (x+1 y+1):5
	
	private Constituency[] consti_array; // Constituency class 배열.
	private int[][] hex_code; //[grid_x][grid_y] 자리에 다각형이 code 몇에 이어지느냐? (-1: 존재하지 않음)
	private float[][] coordi_X; //[grid_x][grid_y] 자리의 x좌표.
	private float[][] coordi_Y; //[grid_x][grid_y] 자리의 y좌표.
	private float[][] surface; //[grid_x][grid_y] 자리의 육각형 배율(닮음비).
	private float[][][] distance; //[grid_x][grid_y]의 n번째 방향 자리의 거리.
	private float distance_avg; //평균 다각형간 거리. : (전체 흰공간)/(전체 다각형 변 길이)
	private float distance_min; //초기배열시, 최소 다각형간 거리.
	private float surface_avg; //평균크기 다각형의 '배율': root mean square
	private float surface_min; //최소크기 다각형의 '배율' 
	private float[] a_dist = {-SQRT_3/2, -SQRT_3/2, 0, 0, SQRT_3/2, SQRT_3/2}; // 각 방위로 1 길이만큼 움직이기 위해 이동해야 하는 +x방향 이동길이.
	private float[] b_dist = {(float)-1/2, (float)1/2, 1, -1, (float)-1/2, (float)1/2}; // 각 방위로 1 길이만큼 움직이기 위해 이동해야 하는 +y방향 이동길이.
	// (x-1 y-1):0 / (x-1 y):1 / (x y-1):3 / (x y+1):2 / (x+1 y):4 / (x+1 y+1):5
	
	private float theta; // 지역구 표시할 때 기울일 각도.
	private float init_center_distance; // 초기 배열시, 각 다각형 중심끼리의 거리.
	private float half_edge_length; // 단위 6각형의 한 변 크기 절반의 길이.
	
	
	
	public int get_gridX_max() {
		return grid_x_max;
	}
	public int get_gridY_max() {
		return grid_y_max;
	}
	public int get_gridX_min() {
		return grid_x_min;
	}
	public int get_gridY_min() {
		return grid_y_min;
	}
	public float get_coordiX(int _gridX, int _gridY) { // gridX, gridY번째 점의 x좌표.
		return coordi_X[_gridX][_gridY];
	}
	public float get_coordiY(int _gridX, int _gridY) { // gridX, gridY번째 점의 y좌표.
		return coordi_Y[_gridX][_gridY];
	}
	private int get_gridX(int _code) { // 코드번호(hex_code[x][y]) _code번째 점의 x좌표.
		return consti_array[_code].get_gridX();
	}
	private int get_gridY(int _code) { // 코드번호(hex_code[x][y]) _code번째 점의 y좌표.
		return consti_array[_code].get_gridY();
	}
	
	public float[] get_a_dist() {
		return a_dist;
	}
	public float get_a_dist(int _bangwooi) {
		return a_dist[_bangwooi];
	}
	public float[] get_b_dist() {
		return b_dist;
	}
	public float get_b_dist(int _bangwooi) {
		return b_dist[_bangwooi];
	}
	
	public float get_half_edge_length() {
		return half_edge_length;
	}
	
	public float get_distance_avg() {
		return distance_avg;
	}
	public float get_distance_min() {
		return distance_min;
	}
	public int[] get_hexCode_x(int _x) { // _x좌표의 hex_code integer array.
		return hex_code[_x];
	}
	public float get_distance(int _x, int _y, int _bangwooi) { // gridX, gridY번째 다각형이 _bangwooi방향 이웃 다각형과 떨어진 거리 (저장한 값).
		return distance[_x][_y][_bangwooi];
	}
	public boolean isExist_grid(int _x, int _y) { // _x,_y 좌표에 다각형(지역구)가 존재하는가?
		if (_x > this.get_gridX_max() || _x < this.get_gridX_min()
				|| _y > this.get_gridY_max() || _y < this.get_gridY_min()) // _x, _y가 최대/최소값 범위를 벗어난다면...
			return false;
		else if (this.get_hexCode_x(_x)[_y] > -1) //그 위치에 실제로 다각형(지역구)가 존재한다면, 이 값이 -1보단 커야지.
			return true;
		else return false;
	}
	
	
	public float get_surface(int _x, int _y) {
		return surface[_x][_y];
	}
	public float get_surface_avg() {
		return surface_avg;
	}
	public float get_surface_min() {
		return surface_min;
	}
	
	public int bangwooi_x(int _x, int _bangwooi) { // x좌표가 gridX인 다각형의 _bangwooi방향 이웃 다각형의 x좌표.
		return _x + (_bangwooi/2 - 1);
	}
	public int bangwooi_y(int _y, int _bangwooi) { // y좌표가 gridX인 다각형의 _bangwooi방향 이웃 다각형의 y좌표.
		return _y + (_bangwooi%3 - 1);
	}
	public int bangwooi_reverse(int _bangwooi) { // _bangwooi 방향의 반댓방향 방위값.
		return 5-_bangwooi;
	}
	public float distance(int x1, int y1, int bangwooi) { // gridX, gridY번째 다각형이 _bangwooi방향 이웃 다각형과 떨어진 거리 (즉석에서 계산한 값).
		int x2 = x1 + (bangwooi/2 - 1);
		int y2 = y1 + (bangwooi%3 - 1);
		return a_dist[bangwooi]*(coordi_X[x2][y2]-coordi_X[x1][y1]) 
				+ b_dist[bangwooi]*(coordi_Y[x2][y2]-coordi_Y[x1][y1])
				- half_edge_length*SQRT_3*(surface[x2][y2]+surface[x1][y1]); // 빼는 값: 두 다각형의 중심~변까지의 수직거리.
	}

	public float vertex_coordiX(int _gridX, int _gridY, int _bangwooi) { // _gridX, _gridY 좌표의 육각형의 꼭지점 X좌표 제시. 왼쪽 위부터 아래로 0 1 2, 오른쪽 위부터 아래로 3 4 5.
		float dx = half_edge_length * this.get_surface(_gridX, _gridY);
		switch (_bangwooi) {
		case 0: dx *= -1; break;
		case 1: dx *= -2; break;
		case 2: dx *= -1; break;
		case 3: dx *= 1; break;
		case 4: dx *= 2; break;
		case 5: dx *= 1; break;
		default: dx = 0; break;
		}
		return this.get_coordiX(_gridX, _gridY) + dx;
	}
	public float vertex_coordiY(int _gridX, int _gridY, int _bangwooi) { // _gridX, _gridY 좌표의 육각형의 꼭지점 Y좌표 제시. 왼쪽 위부터 아래로 0 1 2, 오른쪽 위부터 아래로 3 4 5.
		float dy = half_edge_length * SQRT_3 * this.get_surface(_gridX, _gridY);
		switch (_bangwooi) {
		case 0: dy *= -1; break;
		case 1: dy = 0; break;
		case 2: dy *= 1; break;
		case 3: dy *= -1; break;
		case 4: dy = 0; break;
		case 5: dy *= 1; break;
		default: dy = 0; break;
		}
		return this.get_coordiY(_gridX, _gridY) + dy;
	}
	public void dist_move(int x1, int y1, float _dx, float _dy) { // gridX, gridY번째 다각형을 _dx, _dy만큼 움직임.
		coordi_X[x1][y1] += _dx;
		coordi_Y[x1][y1] += _dy;
		for (int i=0; i<bangwooi_num; i++) 
			distance[x1][y1][i] = this.distance(x1, y1, i);
	}
	public void dist_move(int x1, int y1, float _distance, int bangwooi) { // gridX, gridY번째 다각형을 bangwooi 방향으로 distance만큼 움직임.
		float dx = _distance * a_dist[bangwooi];
		float dy = _distance * b_dist[bangwooi];
		
		coordi_X[x1][y1] += dx;
		coordi_Y[x1][y1] += dy;
		for (int i=0; i<bangwooi_num; i++) 
			distance[x1][y1][i] = this.distance(x1, y1, i);
	}
	
	
	
	public float get_distance_mean() {
		float distance_sum = 0;
		int edge_count = 0;
		for (int i=0; i<=grid_x_max; i++) { //surface를 grid_x, grid_y 기준으로 실제 각 다각형에 맞게 initialize함. (중심이 균일배열되게.)
			for (int j=0; j<=grid_y_max; j++) {
				if (this.isExist_grid(i, j)) {
					for (int k=0; k<bangwooi_num; k++) { // bangwooi를 증가시키면서, distance[][][]를 grid_x, grid_y initialize함. (중심이 균일배열되게.)
						if (this.isExist_grid(bangwooi_x(i, k), bangwooi_y(j, k))) { //거리를 잴 상대위치에 실제로 다각형(지역구)가 존재한다면...
							distance_sum += distance(i, j, k);
							edge_count++;
						}
					}
				}
			}
		}

		return distance_sum / edge_count;
	}

	public float get_distance_STDEV() {
		float distance_mean = this.get_distance_mean();
		float squareDEV_sum = 0;
		int edge_count = 0;
		for (int i=0; i<=grid_x_max; i++) { //surface를 grid_x, grid_y 기준으로 실제 각 다각형에 맞게 initialize함. (중심이 균일배열되게.)
			for (int j=0; j<=grid_y_max; j++) {
				if (this.isExist_grid(i, j)) {
					for (int k=0; k<bangwooi_num; k++) { // bangwooi를 증가시키면서, distance[][][]를 grid_x, grid_y initialize함. (중심이 균일배열되게.)
						if (this.isExist_grid(bangwooi_x(i, k), bangwooi_y(j, k))) { //거리를 잴 상대위치에 실제로 다각형(지역구)가 존재한다면...
							squareDEV_sum += (distance(i, j, k)-distance_mean) * (distance(i, j, k)-distance_mean);
							edge_count++;
						}
					}
				}
			}
		}

		return (float) Math.sqrt(squareDEV_sum/edge_count);
	}
	
	
	
	public void blooming_a() {
		Blooming_Map bm = new Blooming_Map(this);
		bm.blooming_a();
		this.coordi_X = bm.get_coordi_X();
		this.coordi_Y = bm.get_coordi_Y();
	}
	public void blooming_a_kern_a() {
		Blooming_Map bm = new Blooming_Map(this);
		bm.blooming_a();
		bm.kern_a();
		this.coordi_X = bm.get_coordi_X();
		this.coordi_Y = bm.get_coordi_Y();
	}
	public void kern_a() {
		Blooming_Map bm = new Blooming_Map(this);
		bm.kern_a();
		this.coordi_X = bm.get_coordi_X();
		this.coordi_Y = bm.get_coordi_Y();
	}

	
	public void print_map(PShape _hex) {// 다각형을 정해진 크기(surface), 좌표(coordi_X, coordi_Y)화면상에 출력.
	    for (int i=0; i<consti_array.length; i++) { 
	    	
	    	int grid_x = consti_array[i].get_gridX();
	    	int grid_y = consti_array[i].get_gridY();
	    	float coordi_x = coordi_X[grid_x][grid_y];
	    	float coordi_y = coordi_Y[grid_x][grid_y];
	    	float area = consti_array[i].get_surface();
	    	
	    	parent.translate(coordi_x, coordi_y);
	    	parent.rotate(theta); // 지역구 문자열 표시용.
	      
	    	parent.fill(255,255,255);
//	    	parent.text(consti_array[i].get_code(), 0, 0);
	    	parent.text(consti_array[i].get_gridX() + ", " + consti_array[i].get_gridY(), 0, 0);
	      
	    	parent.noStroke();
	    	parent.fill(255,255,255, 100);
	    	parent.scale(area);
	    	parent.shape(_hex, 0, 0);
	    	parent.scale(1/area);
	      
	    	parent.rotate(-theta);
	    	parent.translate(-coordi_x, -coordi_y);
	    }
	    System.out.println("distance_mean: "+this.get_distance_mean());
		System.out.println("distance_STDEV: "+this.get_distance_STDEV());
	}
	
	
	public void print_map_r(PShape _hex) {// 다각형을 정해진 크기(surface), 좌표(coordi_X, coordi_Y)화면상에 출력.
	    for (int i=0; i<consti_array.length; i++) { 
	    	
	    	int grid_x = consti_array[i].get_gridX();
	    	int grid_y = consti_array[i].get_gridY();
	    	float coordi_x = coordi_X[grid_x][grid_y];
	    	float coordi_y = coordi_Y[grid_x][grid_y];
	    	float area = consti_array[i].get_surface();
	    	
	    	parent.translate(coordi_x, coordi_y);
	    	parent.rotate(theta); // 지역구 문자열 표시용.
	      
	    	parent.fill(255,255,255);
//	    	parent.text(consti_array[i].get_code(), 0, 0);
	    	parent.text(consti_array[i].get_gridX() + ", " + consti_array[i].get_gridY(), 0, 0);
	      
	    	parent.noStroke();
	    	parent.fill(255,108,158, 140);
	    	parent.scale(area);
	    	parent.shape(_hex, 0, 0);
	    	parent.scale(1/area);
	      
	    	parent.rotate(-theta);
	    	parent.translate(-coordi_x, -coordi_y);
	    }
	    System.out.println("distance_mean: "+this.get_distance_mean());
		System.out.println("distance_STDEV: "+this.get_distance_STDEV());
	}
	
	public void print_map_g(PShape _hex) {// 다각형을 정해진 크기(surface), 좌표(coordi_X, coordi_Y)화면상에 출력.
	    for (int i=0; i<consti_array.length; i++) { 
	    	
	    	int grid_x = consti_array[i].get_gridX();
	    	int grid_y = consti_array[i].get_gridY();
	    	float coordi_x = coordi_X[grid_x][grid_y];
	    	float coordi_y = coordi_Y[grid_x][grid_y];
	    	float area = consti_array[i].get_surface();
	    	
	    	parent.translate(coordi_x, coordi_y);
	    	parent.rotate(theta); // 지역구 문자열 표시용.
	      
	    	parent.fill(255,255,255);
//	    	parent.text(consti_array[i].get_code(), 0, 0);
	    	parent.text(consti_array[i].get_gridX() + ", " + consti_array[i].get_gridY(), 0, 0);
	      
	    	parent.noStroke();
	    	parent.fill(108,255,158, 140);
	    	parent.scale(area);
	    	parent.shape(_hex, 0, 0);
	    	parent.scale(1/area);
	      
	    	parent.rotate(-theta);
	    	parent.translate(-coordi_x, -coordi_y);
	    }
	    System.out.println("distance_mean: "+this.get_distance_mean());
		System.out.println("distance_STDEV: "+this.get_distance_STDEV());
	}
	
	
	
	public void print_map_b(PShape _hex) {// 다각형을 정해진 크기(surface), 좌표(coordi_X, coordi_Y)화면상에 출력.
	    for (int i=0; i<consti_array.length; i++) { 
	    	
	    	int grid_x = consti_array[i].get_gridX();
	    	int grid_y = consti_array[i].get_gridY();
	    	float coordi_x = coordi_X[grid_x][grid_y];
	    	float coordi_y = coordi_Y[grid_x][grid_y];
	    	float area = consti_array[i].get_surface();
	    	
	    	parent.translate(coordi_x, coordi_y);
	    	parent.rotate(theta); // 지역구 문자열 표시용.
	      
	    	parent.fill(255,255,255);
//	    	parent.text(consti_array[i].get_code(), 0, 0);
	    	parent.text(consti_array[i].get_gridX() + ", " + consti_array[i].get_gridY(), 0, 0);
	      
	    	parent.noStroke();
	    	parent.fill(108,158,255, 140);
	    	parent.scale(area);
	    	parent.shape(_hex, 0, 0);
	    	parent.scale(1/area);
	      
	    	parent.rotate(-theta);
	    	parent.translate(-coordi_x, -coordi_y);
	    }
	    System.out.println("distance_mean: "+this.get_distance_mean());
		System.out.println("distance_STDEV: "+this.get_distance_STDEV());
	}

	
	
	
	Consti_Map (Processing_sketch _p, Constituency[] _consti_array, float _half_edge_length, float _init_center_distance, float _theta) {
		parent = _p;
		theta = _theta;
		
		construct_consti_map(_consti_array, _half_edge_length, _init_center_distance);
	}
	
	
	Consti_Map (Processing_sketch _p, XML[] _xml, float _half_edge_length, float _init_center_distance, float _theta) {

		parent = _p;
		theta = _theta;
		
		XML[] xml_array = _xml;
		Constituency[] consti_arr = new Constituency[xml_array.length];
		
	    for (int i=0; i<xml_array.length; i++) { //xml_array에서 Constituency 배열 만들어냄. 
	    	String aaa = xml_array[i].getChild("upperLocal").getContent();	    		

			XML consti_XML = xml_array[i];
			consti_arr[i] = new Constituency (
	    			parent,
	    			consti_XML.getChild("code").getIntContent(), 
	    			consti_XML.getChild("upperLocal").getContent(), 
	    			consti_XML.getChild("name").getContent(), 
	    			consti_XML.getChild("gridX").getIntContent(), 
	    			consti_XML.getChild("gridY").getIntContent(), 
	    			consti_XML.getChild("popul").getIntContent());
	    }

	    construct_consti_map(consti_arr, _half_edge_length, _init_center_distance);
	}
	
	
	
	
	private void construct_consti_map(Constituency[] _consti_arr, float _half_edge_length, float _init_center_distance) {
		half_edge_length = _half_edge_length;
		init_center_distance = _init_center_distance;
		float dx = init_center_distance/2*SQRT_3;
		float dy = init_center_distance;

		
		consti_array = _consti_arr;
		
		grid_x_max = consti_array[0].get_gridX();
		grid_y_max = consti_array[0].get_gridY();
		grid_x_min = grid_x_max;
		grid_y_min = grid_y_max;
	    
	    for (int i=1; i<consti_array.length; i++) { // grid_x_max, grid_y_max, grid_x_min, grid_y_min 확정.
	    	if (consti_array[i].get_gridX() > grid_x_max) 
	    		grid_x_max = consti_array[i].get_gridX();
	    	else if (consti_array[i].get_gridX() < grid_x_min)
	    		grid_x_min = consti_array[i].get_gridX();
	    	if (consti_array[i].get_gridY() > grid_y_max) 
	    		grid_y_max = consti_array[i].get_gridY(); 	
	    	else if (consti_array[i].get_gridY() < grid_y_min)
	    		grid_y_min = consti_array[i].get_gridY();
	    }
	    
	    hex_code = new int[grid_x_max+1][grid_y_max+1]; 
	    for(int i=0; i<=grid_x_max; i++) { // hexcode[][]를 -1로 초기화: '지역구가 없는 칸'에는 -1이 들어감.
	    	for(int j=0; j<=grid_y_max; j++) {
	    		hex_code[i][j] = -1;
	    	}
	    }
	    coordi_X = new float[grid_x_max+1][grid_y_max+1];
	    coordi_Y = new float[grid_x_max+1][grid_y_max+1];
	    surface = new float[grid_x_max+1][grid_y_max+1];
	    distance = new float[grid_x_max+1][grid_y_max+1][bangwooi_num];
	    
	    for (int i=0; i<=grid_x_max; i++) { //coordi_X, coordi_Y를 grid_x, grid_y 기준으로 initialize함. (중심이 균일배열되게.)
	    	for (int j=0; j<=grid_y_max; j++) {
	    		coordi_X[i][j] = (float) (dx*(float)i);
		    	coordi_Y[i][j] = (float) (dy*(float)(j-i/(float)2));
		    	surface[i][j] = (float) 0.0; //surface를 0.0으로 initialize: '지역구가 없는 칸'에는 0.0이 들어감.
	    	}
	    }
	    	    
	    distance_avg = (float) 0;
	    surface_avg = (float) 0;
	    float surface_count = (float) 0;
	    int edge_count = 0;
	    distance_min = Float.MAX_VALUE;
	    surface_min = Float.MAX_VALUE;
	    
	    for (int i=0; i<consti_array.length; i++) { //surface를 grid_x, grid_y 기준으로 실제 각 다각형에 맞게 initialize함. (중심이 균일배열되게.)
	    	int grid_x = consti_array[i].get_gridX();
	    	int grid_y = consti_array[i].get_gridY();
	    	hex_code[grid_x][grid_y] = consti_array[i].get_code();
	    	surface[grid_x][grid_y] = consti_array[i].get_surface();
	    	if (surface_min > surface[grid_x][grid_y])
	    		surface_min = surface[grid_x][grid_y];
	    	surface_avg += surface[grid_x][grid_y] * surface[grid_x][grid_y];
	    }
	    
	    for (int i=0; i<consti_array.length; i++) {	
	    	int grid_x = consti_array[i].get_gridX();
	    	int grid_y = consti_array[i].get_gridY();
	    	for (int j=0; j<bangwooi_num; j++) { // bangwooi를 증가시키면서, distance[][][]를 grid_x, grid_y initialize함. (중심이 균일배열되게.)
	    		if (this.isExist_grid(bangwooi_x(grid_x, j), bangwooi_y(grid_y, j))) { //거리를 잴 상대위치에 실제로 다각형(지역구)가 존재한다면...
	    			float distance_val = distance(grid_x, grid_y, j);
	    			distance[grid_x][grid_y][j] = distance_val;
	    			distance_avg += (surface[grid_x][grid_y]+surface[bangwooi_x(grid_x, j)][bangwooi_y(grid_y, j)])*distance_val/2;
	    			surface_count += surface[grid_x][grid_y];
	    			edge_count++;
	    			if (distance_min > distance_val)
	    				distance_min = distance_val;
	    		}
	    	}
	    }
	    
	    surface_avg = (float) Math.sqrt(surface_avg/consti_array.length);
	    distance_avg = distance_avg / surface_count; // distance_avg: 평균!
	   
	    

	}
}
