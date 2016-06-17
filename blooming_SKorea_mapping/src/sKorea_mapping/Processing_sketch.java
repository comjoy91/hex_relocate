package sKorea_mapping;

import processing.core.*;
import processing.data.*;

public class Processing_sketch extends PApplet {


	static XML xml;
	static PFont myFont;
	
	
	
	//디스플레이할 방법 모드: 점복자/6각형, 같은크기/인구비례크기.
	static final int SAME_HEX = 1;
	static final int SAME_PLOT = 2;
	static final int POPUL_HEX = 3;
	static final int POPUL_PLOT = 4;

	static int width = 1800;
	static int height = 1010;
	static float origin_x = (float)100;
	static float origin_y = (float)200;
	static float theta = -PI/3; // 지역구 표시할 때 기울일 각도.
	
	static float popul_perPlot = 4000; // 점복자 1개당 인구
	static float popul_div = 42; // 단위 6각형의 점복자 개수.
	
	static float half_edge_length = (float)9; // 단위 6각형의 한 변 크기 절반의 길이.
	static float init_center_distance = (float)45; //초기 6각형 중심 사이의 거리.
	
	static Consti_Map map;


	
	public void print_map (float _origin_x, float _origin_y, Consti_Map _map, PShape _hex) {
		translate(_origin_x, _origin_y);
		_map.print_map(_hex);
	}
	public void print_map_r (float _origin_x, float _origin_y, Consti_Map _map, PShape _hex) {
		translate(_origin_x, _origin_y);
		_map.print_map_r(_hex);
	}
	public void print_map_g (float _origin_x, float _origin_y, Consti_Map _map, PShape _hex) {
		translate(_origin_x, _origin_y);
		_map.print_map_g(_hex);
	}
	public void print_map_b (float _origin_x, float _origin_y, Consti_Map _map, PShape _hex) {
		translate(_origin_x, _origin_y);
		_map.print_map_b(_hex);
	}
	
	public void hexMaking(PShape _hex) {
		_hex.beginShape();
		_hex.vertex(half_edge_length, half_edge_length*sqrt(3));
	    _hex.vertex(half_edge_length*2, 0);
	    _hex.vertex(half_edge_length, -half_edge_length*sqrt(3));
	    _hex.vertex(-half_edge_length, -half_edge_length*sqrt(3));
	    _hex.vertex(-half_edge_length*2, 0);
	    _hex.vertex(-half_edge_length, half_edge_length*sqrt(3));
	    _hex.endShape(CLOSE); // hex: 단위 6각형. 한 변의 길이 = 2 * half_edge_length, 점복자 개수 = popul_div. 만들어냈습니다. 
	    _hex.setStroke(false);
	}
	

	
	public void settings() {
		size(width, height, FX2D);
	}
	
	public void setup() {

		background(0);
		myFont = createFont("NotoSansKR-Regular", 10);
		xml = loadXML("2012_constituency.xml");
		textFont(myFont);
//		PShape hex = loadShape("hexagon_42.svg");
		PShape hex = createShape();
		hexMaking(hex); // hex: 단위 6각형. 한 변의 길이 = 2 * half_edge_length, 점복자 개수 = popul_div. 만들어냈습니다. 
		hex.disableStyle();
		
		
		textAlign(CENTER, CENTER);
		
		map = new Consti_Map(this, xml.getChildren("consti"), half_edge_length, init_center_distance, theta);	
		map.kern_a();
		print_map_b (origin_x, origin_y, map, hex);
//		print_map (7*origin_x, 0, map, hex);
		map = new Consti_Map(this, xml.getChildren("consti"), half_edge_length, init_center_distance, theta);
		map.blooming_a_kern_a();
		print_map_g (7*origin_x, 0, map, hex);
		
/*		
//		pprint_map (0,0, map, hex);
		
		map = new Consti_Map(this, xml.getChildren("consti"), half_edge_length, init_center_distance, theta);
//		print_map (7*origin_x, 0, map, hex);
		map.blooming_c();
//		map.blooming_int(30);

*/
//		pprint_map (7*origin_x, 0, map, hex);

	    



	}
	
	
	public void draw() {
		
		
		
	}
	
	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "sKorea_mapping.Processing_sketch" });
	}
	


	
}