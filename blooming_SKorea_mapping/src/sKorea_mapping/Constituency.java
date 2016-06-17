package sKorea_mapping;

public class Constituency {
	
	Processing_sketch parent;

	int code;
	String upperLocal; //광역자치단체 이름.
	String name;
	int grid_x; //X좌표_정수값.
	int grid_y; //Y좌표_정수값.
	int population; //인구. 
	
	int num_plot; //점복자 개수.
	float surface; //면적.
	

	public int get_code() {
		return code;
	}
	public String get_name() {
		return name;
	}
	public int get_gridX() {
		return grid_x;
	}
	public int get_gridY() {
		return grid_y;
	}
	public float get_surface() {
		return surface;
	}
	
	Constituency (Processing_sketch _p, int _code, String _upperLocal, String _name, int _grid_x, int _grid_y, int _popul) {
		
		parent = _p;
		
		code = _code;
		upperLocal = _upperLocal;
		name = _name;
		grid_x = _grid_x;
		grid_y = _grid_y;
		population = _popul;
		
		num_plot = parent.round((float)population / parent.popul_perPlot);
		
//    	surface = 1;
		surface = parent.sqrt((float)(parent.round((float)num_plot/(float)6)*6) / parent.popul_div); // 6각형 면적은 점복자 모드 때의 면적으로 할 것이냐, 절대적인 인구비례로 할 것인가.
	}
	
}
