package sKorea_mapping;

import processing.core.*;

public class Blooming_Map {
	
	public static float SQRT_3 = (float)Math.sqrt(3);
	
	public Consti_Map parent_map; // 부모 Consti_Map

	private int grid_x_max; //최대 grid_X
	private int grid_x_min; //최소 grid_X
	private int grid_y_max; //최대 grid_y
	private int grid_y_min; //최소 grid_y
	public int bangwooi_num; //총 방위개수.
	// (x-1 y-1):0 / (x-1 y):1 / (x y-1):3 / (x y+1):2 / (x+1 y):4 / (x+1 y+1):5

	private float[][] coordi_X; //[grid_x][grid_y] 자리의 x좌표.
	private float[][] coordi_Y; //[grid_x][grid_y] 자리의 y좌표.
	private float surface_init; //최초 다각형 배율.
//	private float[][][] distance; //[grid_x][grid_y]의 n번째 방향 자리의 거리.
	private float distance_init; //최초 다각형간 간격.
	private float[] a_dist; // 각 방위로 1 길이만큼 움직이기 위해 이동해야 하는 +x방향 이동길이.
	private float[] b_dist; // 각 방위로 1 길이만큼 움직이기 위해 이동해야 하는 +y방향 이동길이.
	private PVector[] normal_dist; //a_dist, b_dist로 만든 단위 벡터 모임/
	// (x-1 y-1):0 / (x-1 y):1 / (x y-1):3 / (x y+1):2 / (x+1 y):4 / (x+1 y+1):5
	
	private float init_center_distance; // 초기 배열시, 각 다각형 중심끼리의 거리.
	private float half_edge_length; // 단위 6각형의 한 변 크기 절반의 길이.
	
	
	public float[][] get_coordi_X() {
		return coordi_X;
	}
	public float[][] get_coordi_Y() {
		return coordi_Y;
	}
	
	private void dist_move(int x1, int y1, float _dx, float _dy) { // gridX, gridY번째 다각형을 _dx, _dy만큼 움직임.
		coordi_X[x1][y1] += _dx;
		coordi_Y[x1][y1] += _dy;
	}
	private void dist_move(int x1, int y1, float _distance, int bangwooi) { // gridX, gridY번째 다각형을 bangwooi 방향으로 distance만큼 움직임.
		float dx = _distance * a_dist[bangwooi];
		float dy = _distance * b_dist[bangwooi];
		
		coordi_X[x1][y1] += dx;
		coordi_Y[x1][y1] += dy;
	}
	
	private int bangwooi_x(int _x, int _bangwooi) {
		return this.parent_map.bangwooi_x(_x, _bangwooi);
	}
	private int bangwooi_y(int _y, int _bangwooi) {
		return this.parent_map.bangwooi_y(_y, _bangwooi);
	}
	
	private float get_surface(int _x, int _y) {
		return this.parent_map.get_surface(_x, _y);
	}
	private float distance(int x1, int y1, int bangwooi) { // gridX, gridY번째 다각형이 _bangwooi방향 이웃 다각형과 떨어진 거리 (즉석에서 계산한 값).
		int x2 = bangwooi_x(x1, bangwooi);
		int y2 = bangwooi_y(y1, bangwooi);
		return a_dist[bangwooi]*(coordi_X[x2][y2]-coordi_X[x1][y1]) 
				+ b_dist[bangwooi]*(coordi_Y[x2][y2]-coordi_Y[x1][y1])
				- half_edge_length*SQRT_3*(get_surface(x2, y2)+get_surface(x1, y1)); // 빼는 값: 두 다각형의 중심~변까지의 수직거리.
	}
	private float neighbour_surface_mean(int _x, int _y) {
		float surface_sum = 0;
		int hex_count = 0;
		for (int bang=0; bang<bangwooi_num; bang++)
			if (this.parent_map.isExist_grid(bangwooi_x(_x, bang), bangwooi_y(_y, bang))) {
				surface_sum += Math.pow(this.get_surface(bangwooi_x(_x, bang), bangwooi_y(_y, bang)), 2);
				hex_count++;
			}
		return (float)Math.sqrt(surface_sum/hex_count);				
	}
	private float NOTExist_surface(int _x, int _y) {
		return neighbour_surface_mean(_x, _y);
	}
	private float hex_distance_avg(int _x, int _y) {
		float distance_sum = 0;
		int dist_count = 0;
		for (int bang=0; bang<bangwooi_num; bang++)
			if (this.parent_map.isExist_grid(bangwooi_x(_x, bang), bangwooi_y(_y, bang))) {
				distance_sum += this.distance(_x, _y, bang);
				dist_count++;
			}
		if (dist_count == 0) return 0;
		else return distance_sum/dist_count;	
	}
	private float NOTExist_distance(int _x, int _y, int _bang) {
		return hex_distance_avg(_x, _y);
	}
	
	
	private void initial_coordinate(float _center_distance) {
		float dx = this.init_center_distance/2*SQRT_3;
		float dy = this.init_center_distance;
		this.coordi_X = new float[grid_x_max+1][grid_y_max+1]; //[grid_x][grid_y] 자리의 x좌표.
	    this.coordi_Y = new float[grid_x_max+1][grid_y_max+1]; //[grid_x][grid_y] 자리의 y좌표.
	    for (int i=0; i<=grid_x_max; i++) { //coordi_X, coordi_Y를 grid_x, grid_y 기준으로 initialize함. (중심이 균일배열되게.)
	    	for (int j=0; j<=grid_y_max; j++) {
	    		this.coordi_X[i][j] = (float) (dx*(float)i);
		    	this.coordi_Y[i][j] = (float) (dy*(float)(j-i/(float)2));
	    	}
	    }
	}
	
	
	
	public void blooming_int(int _times) {
		if (_times<1);
		else if (_times==1) 
			blooming_a();
		else {
			blooming_int(_times-1);
			blooming_a();
		}
	}
	
	
/*	
	public void blooming_aa() {
		int i_, j_;
		float move_dist;
		for (int i=0; i<=grid_x_max; i++) {
	    	for (int j=0; j<=grid_y_max; j++) { // i, j: 기준점(blooming될) 다각형의 정수좌표. i_, j_: blooming하면서 이동할 다각형의 정수좌표. k: k의 최대/최소 제한을 명기하는 변수.
	    			    		
	    		if (this.parent_map.isExist_grid(i, j)) {
		    		move_dist = (get_surface(i, j) - surface_init) * half_edge_length * SQRT_3 / 3;
		    		for (int bang=0; bang<bangwooi_num; bang++) {
		    			i_ = bangwooi_x(i, bang);
		    			j_ = bangwooi_y(j, bang);
		    			if (this.parent_map.isExist_grid(i_, j_))
		    				dist_move(i_, j_, move_dist, bang);
		    		}
	    		}
	    	}
		}

	}
*/	
	
	public void blooming_a() {
		int i_, j_, k;
		float move_dist;
		for (int i=0; i<=grid_x_max; i++) {
			for (int j=0; j<=grid_y_max; j++) { // i, j: 기준점(blooming될) 다각형의 정수좌표. i_, j_: blooming하면서 이동할 다각형의 정수좌표. k: k의 최대/최소 제한을 명기하는 변수.

				if (this.parent_map.isExist_grid(i, j)) {
					move_dist = (get_surface(i, j) - surface_init) * half_edge_length * SQRT_3 / 3;

					for (i_=i-1; i_>=grid_x_min; i_--) {
						k = j - (i-i_);
						
						dist_move(i_, j, move_dist, 1);
						if (k>=grid_y_min)
							dist_move(i_, k, move_dist, 0);
						
						for (j_=j-1; (j_>k && j_>=grid_y_min); j_--) {
							int div_c = j-k;
							dist_move(i_, j_, move_dist/div_c*(j_-k)/2, 1);
							dist_move(i_, j_, move_dist/div_c*(j-j_)/2, 0);  				
						}
						for (j_=k-1; j_>=grid_y_min; j_--) {
							int div_c = j-j_;
							dist_move(i_, j_, move_dist/div_c*(i-i_)/2, 0);
							dist_move(i_, j_, move_dist/div_c*(div_c-(i-i_))/2, 3);
						}
						for (j_=j+1; j_<=grid_y_max; j_++) {
							int div_c = (i-i_)+(j_-j);
							dist_move(i_, j_, move_dist/div_c*(i-i_)/2, 1);
							dist_move(i_, j_, move_dist/div_c*(j_-j)/2, 2);
						}
					}
					for (i_=i+1; i_<=grid_x_max; i_++) {
						k = j + (i_-i);
						
						dist_move(i_, j, move_dist, 4);
						if (k<=grid_y_max)
							dist_move(i_, k, move_dist, 5);
						
						for (j_=j+1; (j_<k && j_<=grid_y_max); j_++) {
							int div_c = k-j;
							dist_move(i_, j_, move_dist/div_c*(k-j_)/2, 4);
							dist_move(i_, j_, move_dist/div_c*(j_-j)/2, 5);
						}
						for (j_=j-1; j_>=grid_y_min; j_--) {
							int div_c = (i_-i)+(j-j_);
							dist_move(i_, j_, move_dist/div_c*(i_-i)/2, 4);
							dist_move(i_, j_, move_dist/div_c*(j-j_)/2, 3);
						}
						for (j_=k+1; j_<=grid_y_max; j_++) {
							int div_c = j_-j;
							dist_move(i_, j_, move_dist/div_c*(i_-i)/2, 5);
							dist_move(i_, j_, move_dist/div_c*(div_c-(i_-i))/2, 2);
						}	    				
					}
					for (j_=j-1; j_>=grid_y_min; j_--) 
						dist_move(i, j_, move_dist, 3);
					for (j_=j+1; j_<=grid_y_max; j_++) 
						dist_move(i, j_, move_dist, 2);
				}
			}
		}
	}
	
	
	
	public void blooming_aaaa() {
		int i_, j_, k;
		float move_dist;
		for (int i=0; i<=grid_x_max; i++) {
			for (int j=0; j<=grid_y_max; j++) { // i, j: 기준점(blooming될) 다각형의 정수좌표. i_, j_: blooming하면서 이동할 다각형의 정수좌표. k: k의 최대/최소 제한을 명기하는 변수.

				if (this.parent_map.isExist_grid(i, j)) {
					move_dist = (get_surface(i, j) - surface_init) * half_edge_length * SQRT_3 / 6;

					for (i_=i-1; i_>=grid_x_min; i_--) { 		
						k = j - (i-i_);
						
						dist_move(i_, j, move_dist, 1);
						if (k>=grid_y_min)
							dist_move(i_, k, move_dist, 0);

						for (j_=j-1; (j_>k && j_>=grid_y_min); j_--) 
							dist_move(i_, j_, move_dist, 0);
						for (j_=k-1; j_>=grid_y_min; j_--) 
							dist_move(i_, j_, move_dist, 3);
						for (j_=j+1; j_<=grid_y_max; j_++) 
							dist_move(i_, j_, move_dist, 1);
					}
					for (i_=i+1; i_<=grid_x_max; i_++) {
						k = j - (i-i_);
						
						dist_move(i_, j, move_dist, 4);
						if (k<=grid_y_max)
							dist_move(i_, k, move_dist, 5);

						for (j_=j+1; (j_<k && j_<=grid_y_max); j_++) 
							dist_move(i_, j_, move_dist, 4);
						for (j_=j-1; j_>=grid_y_min; j_--) 
							dist_move(i_, j_, move_dist, 5);
						for (j_=k+1; j_<=grid_y_max; j_++) 
							dist_move(i_, j_, move_dist, 2);	    				
					}
					for (j_=j-1; j_>=grid_y_min; j_--)
						dist_move(i, j_, move_dist, 3);
					for (j_=j+1; j_<=grid_y_max; j_++)
						dist_move(i, j_, move_dist, 2);
				}
			}
		}
	}
	
	
	
	public void blooming_aaa() {
		int i_, j_;
		float move_dist;
		for (int i=0; i<=grid_x_max; i++) {
			for (int j=0; j<=grid_y_max; j++) { // i, j: 기준점(blooming될) 다각형의 정수좌표. i_, j_: blooming하면서 이동할 다각형의 정수좌표.

				if (this.parent_map.isExist_grid(i, j)) {
					move_dist = (get_surface(i, j) - surface_init) * half_edge_length * SQRT_3 / 3;

					j_ = j-1;
					for (i_=i-1; (i_>=grid_x_min && j_>=grid_y_min); i_--) {
						j_ = j - (i-i_);
						dist_move(i_, j, move_dist, 1);
						dist_move(i_, j_, move_dist, 0);
						j_--;
					}
					
					j_ = j+1;
					for (i_=i+1; (i_<=grid_x_max && j_<=grid_y_max); i_++) {
						dist_move(i_, j, move_dist, 4);
						dist_move(i_, j_, move_dist, 5);
						j_++;	    				
					}
					
					for (j_=j-1; j_>=grid_y_min; j_--)
						dist_move(i, j_, move_dist, 3);
					for (j_=j+1; j_<=grid_y_max; j_++)
						dist_move(i, j_, move_dist, 2);
				}
			}
		}
	}
	
	
	
/*	
	public void blooming_b() {
		int i_, j_, k;
		float move_dist;
		for (int i=0; i<=grid_x_max; i++) {
	    	for (int j=0; j<=grid_y_max; j++) { // i, j: 기준점(blooming될) 다각형의 정수좌표. i_, j_: blooming하면서 이동할 다각형의 정수좌표. k: k의 최대/최소 제한을 명기하는 변수.

	    		
	    		
	    		if (this.parent_map.isExist_grid(i, j)) {
		    		move_dist = (get_surface(i, j)/surface_init - 1) * half_edge_length * SQRT_3 / 3;

	    			for (i_=i-1; i_>=grid_x_min; i_--) {
	    			k = j - (i-i_);
	    				if (this.parent_map.isExist_grid(i_, j))
	    					dist_move(i_, j, move_dist, 1);
	    				if (this.parent_map.isExist_grid(i_, k))
	    					dist_move(i_, k, move_dist, 0);
	    				for (j_=j-1; j_>k; j_--) 
	    					if (this.parent_map.isExist_grid(i_, j_)) {
	    						dist_move(i_, j_, move_dist/SQRT_3, 0);
	    						dist_move(i_, j_, move_dist/SQRT_3, 1);
	    					}
	    				for (j_=k-1; j_>=grid_y_min; j_--) 
	    					if (this.parent_map.isExist_grid(i_, j_)) {
	    						dist_move(i_, j_, move_dist/SQRT_3, 0);
	    						dist_move(i_, j_, move_dist/SQRT_3, 3);
	    					}
	    				for (j_=j+1; j_<=grid_y_max; j_++) 
	    					if (this.parent_map.isExist_grid(i_, j_)) {
	    						dist_move(i_, j_, move_dist/SQRT_3, 1);
	    						dist_move(i_, j_, move_dist/SQRT_3, 2);
	    					}
	    			}
	    			for (i_=i+1; i_<=grid_x_max; i_++) {
	    			k = j - (i-i_);
	    				if (this.parent_map.isExist_grid(i_, j))
	    					dist_move(i_, j, move_dist, 4);
	    				if (this.parent_map.isExist_grid(i_, k))
	    					dist_move(i_, k, move_dist, 5);
	    				for (j_=j+1; j_<k; j_++) 
	    					if (this.parent_map.isExist_grid(i_, j_)) {
	    						dist_move(i_, j_, move_dist/SQRT_3, 4);
	    						dist_move(i_, j_, move_dist/SQRT_3, 5);
	    					}
	    				for (j_=j-1; j_>=grid_y_min; j_--) 
	    					if (this.parent_map.isExist_grid(i_, j_)) {
	    						dist_move(i_, j_, move_dist/SQRT_3, 3);
	    						dist_move(i_, j_, move_dist/SQRT_3, 4);
	    					}
	    				for (j_=k+1; j_<=grid_y_max; j_++) 
	    					if (this.parent_map.isExist_grid(i_, j_)) {
	    						dist_move(i_, j_, move_dist/SQRT_3, 2);
	    						dist_move(i_, j_, move_dist/SQRT_3, 5);
	    					}
	    			}
	    			for (k=j-1; k>=grid_y_min; k--) {
	    				if (this.parent_map.isExist_grid(i, k)) 
	    					dist_move(i, k, move_dist, 3);
	    			}
	    			for (k=j+1; k<=grid_y_max; k++) {
	    				if (this.parent_map.isExist_grid(i, k))
	    					dist_move(i, k, move_dist, 2);
	    			}
	    		} 
	    	}
	    }
	}
	
*/	
	
	public void blooming_c() {
		int i_, j_, k;
		float move_dist;
		PVector vec = new PVector((float)0.0, (float)0.0);
		PVector vec_ = new PVector((float)0.0, (float)0.0);
		for (int i=0; i<=grid_x_max; i++) {
			for (int j=0; j<=grid_y_max; j++) { // i, j: 기준점(blooming될) 다각형의 정수좌표. i_, j_: blooming하면서 이동할 다각형의 정수좌표. k: k의 최대/최소 제한을 명기하는 변수.

				if (this.parent_map.isExist_grid(i, j)) {
					move_dist = (get_surface(i, j)/surface_init - 1) * half_edge_length * SQRT_3 / 3;

					for (i_=i-1; i_>=grid_x_min; i_--) {
						k = j - (i-i_);
						
						dist_move(i_, j, move_dist, 1);
						if (k>=grid_y_min)
							dist_move(i_, k, move_dist, 0);
						
						for (j_=j-1; (j_>k && j_>=grid_y_min); j_--) {
							int div_c = j-k;
							vec.set(this.normal_dist[1]).mult((j_-k)/div_c);
							vec_.set(this.normal_dist[0]).mult((j-j_)/div_c);
							vec.add(vec_).normalize().mult(move_dist);
							dist_move(i_, j_, vec.x, vec.y);
						}
						for (j_=k-1; j_>=grid_y_min; j_--) { 
							int div_c = j-j_;
							vec.set(this.normal_dist[0]).mult((i-i_)/div_c);
							vec_.set(this.normal_dist[3]).mult((div_c-(i-i_))/div_c);
							vec.add(vec_).normalize().mult(move_dist);
							dist_move(i_, j_, vec.x, vec.y);
						}
						for (j_=j+1; j_<=grid_y_max; j_++) {
							int div_c = (i-i_)+(j_-j);
							vec.set(this.normal_dist[1]).mult((i-i_)/div_c);
							vec_.set(this.normal_dist[0]).mult((j_-j)/div_c);
							vec.add(vec_).normalize().mult(move_dist);
							dist_move(i_, j_, vec.x, vec.y);
						}
					}
					for (i_=i+1; i_<=grid_x_max; i_++) {
						k = j - (i-i_);
						
						dist_move(i_, j, move_dist, 4);
						if (k<=grid_y_max)
							dist_move(i_, k, move_dist, 5);
						
						for (j_=j+1; (j_<k && j_<=grid_y_max); j_++)  {
							int div_c = k-j;
							vec.set(this.normal_dist[4]).mult((k-j_)/div_c);
							vec_.set(this.normal_dist[5]).mult((j_-j)/div_c);
							vec.add(vec_).normalize().mult(move_dist);
							dist_move(i_, j_, vec.x, vec.y);
						}
						for (j_=j-1; j_>=grid_y_min; j_--) {
							int div_c = (i_-i)+(j-j_);
							vec.set(this.normal_dist[4]).mult((i_-i)/div_c);
							vec_.set(this.normal_dist[3]).mult((j-j_)/div_c);
							vec.add(vec_).normalize().mult(move_dist);
							dist_move(i_, j_, vec.x, vec.y);
						}
						for (j_=k+1; j_<=grid_y_max; j_++) {
							int div_c = j_-j;
							vec.set(this.normal_dist[5]).mult((i_-i)/div_c);
							vec_.set(this.normal_dist[2]).mult((div_c-(i_-i))/div_c);
							vec.add(vec_).normalize().mult(move_dist);
							dist_move(i_, j_, vec.x, vec.y);
						}
					}
					for (j_=j-1; j_>=grid_y_min; j_--)
						dist_move(i, j_, move_dist, 3);
					for (j_=j+1; j_<=grid_y_max; j_++) 
						dist_move(i, j_, move_dist, 2);
				}
			}
		}
	}

	
	
	
	
	public void kern_a() {
		
		float[][][] distance_arr = new float[grid_x_max+1][grid_y_max+1][bangwooi_num];
		for (int i=0; i<=grid_x_max; i++) {
			for (int j=0; j<=grid_y_max; j++) {
				if (this.parent_map.isExist_grid(i, j))
					for (int k=0; k<bangwooi_num; k++) {
						if (this.parent_map.isExist_grid(bangwooi_x(i, k), bangwooi_y(j, k)))
								distance_arr[i][j][k] = this.distance(i, j, k);
						else
							distance_arr[i][j][k] = this.NOTExist_distance(i, j, k);
					}
				else
					for (int k=0; k<bangwooi_num; k++)
						distance_arr[i][j][k] = 0;
			}
		}
		int i_, j_;
		float move_dist;
		for (int i=grid_x_min; i<=grid_x_max; i++) {
			for (int j=grid_y_min; j<=grid_y_max; j++) { // i, j: 기준점(blooming될) 다각형의 정수좌표. i_, j_: blooming하면서 이동할 다각형의 정수좌표.

				if (this.parent_map.isExist_grid(i, j)) {

					move_dist = (distance_arr[i][j][0] - distance_arr[i][j][5]) / 2;
					for (i_=i-1; i_>=grid_x_min; i_--) {
						j_ = j - (i-i_);
						if (this.parent_map.isExist_grid(i_, j_))
							dist_move(i_, j_, move_dist, 5);
						else break;
					}
					for (i_=i+1; i_<=grid_x_max; i_++) {
						j_ = j - (i-i_);
						if (this.parent_map.isExist_grid(i_, j_))
							dist_move(i_, j_, move_dist, 5);
						else break;
					}

					move_dist = (distance_arr[i][j][1] - distance_arr[i][j][4]) / 2;
					for (i_=i-1; i_>=grid_x_min; i_--) {
						if (this.parent_map.isExist_grid(i_, j))
							dist_move(i_, j, move_dist, 4);
						else break;
					}
					for (i_=i+1; i_<=grid_x_max; i_++) {
						if (this.parent_map.isExist_grid(i_, j))
							dist_move(i_, j, move_dist, 4);
						else break;
					}

					move_dist = (distance_arr[i][j][2] - distance_arr[i][j][3]) / 2;
					for (j_=j-1; j_>=grid_y_min; j_--) {
						if (this.parent_map.isExist_grid(i, j_))
							dist_move(i, j_, move_dist, 3);
						else break;
					}
					for (j_=j+1; j_<=grid_y_max; j_++) {
						if (this.parent_map.isExist_grid(i, j_))
							dist_move(i, j_, move_dist, 3);
						else break;
					}
					
/*					move_dist = (distance_arr[i][j][0] - distance_arr[i][j][5]) / 2;
					for (i_=grid_x_min; i_<=grid_x_max; i_++) {
						j_ = j - (i-i_);
						if (j_>=grid_y_min && j_<=grid_y_max)
							dist_move(i_, j_, move_dist, 5);
					}
					dist_move(i, j, -move_dist, 5);

					move_dist = (distance_arr[i][j][1] - distance_arr[i][j][4]) / 2;
					for (i_=grid_x_min; i_<=grid_x_max; i_++)
						dist_move(i_, j, move_dist, 4);
					dist_move(i, j, -move_dist, 4);

					move_dist = (distance_arr[i][j][2] - distance_arr[i][j][3]) / 2;
					for (j_=grid_y_min; j_<=grid_y_max; j_++)
						dist_move(i, j_, move_dist, 3);
					dist_move(i, j, -move_dist, 3);
*/
				}
			}
		}	
	}


	
	
	Blooming_Map (Consti_Map _p) {

		parent_map = _p;
		
		grid_x_max = _p.get_gridX_max(); //최대 grid_X
		grid_x_min = _p.get_gridX_min(); //최소 grid_X
		grid_y_max = _p.get_gridY_max(); //최대 grid_y
		grid_y_min = _p.get_gridY_min(); //최소 grid_y
		bangwooi_num = _p.bangwooi_num; //총 방위개수.

		surface_init = _p.get_surface_avg(); //최초 다각형 배율.
		distance_init = _p.get_distance_avg(); //최초 다각형간(경계간) 간격 == 초기 parent_map에서의 간격. 나중에 업데이트됨.
		a_dist = _p.get_a_dist(); // 각 방위로 1 길이만큼 움직이기 위해 이동해야 하는 +x방향 이동길이.
		b_dist = _p.get_b_dist(); // 각 방위로 1 길이만큼 움직이기 위해 이동해야 하는 +y방향 이동길이.
		normal_dist = new PVector[this.a_dist.length];
		for(int i=0; i<normal_dist.length; i++) 
			normal_dist[i] = new PVector(this.a_dist[i], this.b_dist[i]);

		half_edge_length = _p.get_half_edge_length(); // 단위 6각형의 한 변 크기 절반의 길이.
		init_center_distance = (surface_init*half_edge_length*SQRT_3)*2 + distance_init; // 초기 배열시, 각 다각형 중심끼리의 거리.
		
		this.initial_coordinate(init_center_distance);
//	    this.blooming();
	}

	
	
}
