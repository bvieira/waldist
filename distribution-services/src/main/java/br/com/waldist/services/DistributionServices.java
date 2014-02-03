package br.com.waldist.services;

import java.util.List;

import br.com.waldist.pojo.DistributionCenter;
import br.com.waldist.pojo.DistributionMap;
import br.com.waldist.pojo.Route;
import br.com.waldist.services.DistributionMapQueue.Content;

public interface DistributionServices
{

	/**
	 * lista o nome dos mapas cadastrados
	 * @return lista com o nome dos mapas
	 */
	List<String> listMaps();

	DistributionMap get(String mapName);
	
	/**
	 * adiciona na fila para criar ou sobreescreve o {@link DistributionMap} existente de acordo com o conteudo enviado no formato [inicio fim distancia] separados '\n', <br/>
	 * ex:<br/> 
	 * A B 10<br/>
	 * B C 5<br/>
	 * @param content
	 */
	void addToQueue(Content content);

	/**
	 * cria ou sobreescreve o {@link DistributionMap} existente de acordo com o conteudo enviado no formato [inicio fim distancia] separados '\n', <br/>
	 * ex:<br/> 
	 * A B 10<br/>
	 * B C 5<br/>
	 * @param content
	 */
	void add(Content content);

	/**
	 * remove o conteúdo da base
	 */
	void removeAll();

	/**
	 * remove o {@link DistributionMap} que contenha o nome enviado
	 */
	void remove(String mapName);

	/**
	 * Busca o menor caminho entre dois {@link DistributionCenter} presentes no {@link DistributionMap} 
	 * @param mapName nome do {@link DistributionMap}
	 * @param start {@link DistributionCenter} de início
	 * @param end {@link DistributionCenter} de fim
	 * @param price preço do combustível
	 * @param performance rendimento em km/l do veículo utilizado
	 * @return {@link Route} rota de menor caminho entre os centros de distribuicao
	 */
	Route search(DistributionCenter start, DistributionCenter end, float price, float performance);

}