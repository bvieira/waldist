package br.com.waldist.dao;

import java.util.List;

import br.com.waldist.pojo.DistributionCenter;
import br.com.waldist.pojo.DistributionMap;
import br.com.waldist.pojo.Path;

public interface DistributionMapDAO
{
	//--------- all distributions maps
	
	/**
	 * @return lista com os nomes de todas as malhas logísticas cadastradas 
	 */
	List<String> listMapNames();
	
	/**
	 * remove todos as malhas cadastradas na base
	 */
	void remove();
	
	
	//--------- distribution map
	DistributionMap get(String mapName);
	
	void save(DistributionMap distributionMap);
	
	void remove(String mapName);
	
	
	//--------- search path
	/**
	 * calcula a rota de menor custo entre os dois {@link DistributionCenter}
	 * @return lista de {@link Path} que representa essa rota
	 */
	List<Path> getSmallestRoute(DistributionCenter start, DistributionCenter end);

}
