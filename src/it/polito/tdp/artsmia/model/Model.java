package it.polito.tdp.artsmia.model;

import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;


import it.polito.tdp.artsmia.db.ArtsmiaDAO;

public class Model {
	
	private List<ArtObject> artObjects; //mi serve elenco degli oggetti
	private Graph<ArtObject,DefaultWeightedEdge> graph; //poi costruisco grafo con quelli
	
	
	/**
	 * Popola la lista artObjects (leggendo dal DB) e crea il grafo
	 */
	public void creaGrafo() {
		
		//1) leggo lista oggetti da DB 
		//(ogni volta che chiamo il metodo butta via lista precedente e ne crea una nuova leggendo da db)
		ArtsmiaDAO dao = new ArtsmiaDAO();
		this.artObjects = dao.listObjects();
		
		//2) creo oggetto grafo (ogni volta che chiamo metodo crea grafo nuovo)
		this.graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//3) aggiungo vertici ( = tutti gli oggetti)
//		for (ArtObject ao : this.artObjects) {
//			this.graph.addVertex(ao); 
//		}
//		//adesso ao fa parte di un set =>IMPORTANTE DEFINIRE HASHCODE e EQUALS in ArtObject
		Graphs.addAllVertices(this.graph, this.artObjects);
		
		//4) aggiungo archi con il loro peso
		for(ArtObject ao : this.artObjects) {
			
			List<ArtObjectAndCount> connessi = dao.listArtObjectAndCount(ao);
			
			for(ArtObjectAndCount c: connessi) {
				ArtObject dest = new ArtObject( c.getArtObjectId(), null, null, null, 0, null, null, null, null, null, 0, null, null, null, null, null);
				//oggetto debole, meglio fare una mappa all'inizio e inizializzo quando prendo artObjects dal dao
				Graphs.addEdge(this.graph, ao,dest,c.getCount());
				System.out.format("(%d,%d) peso %d\n",ao.getId(),dest.getId(),c.getCount());
			}
		}
		
		/*VERSIONE 1:  NON EFFICIENTE
 * 		for(ArtObject aop: this.artObjects) {
 
			for(ArtObject aoa: this.artObjects) {
				if(!aop.equals(aoa) && aop.getId()<aoa.getId()) { 
				//escludo coppie (ao,ao) per togliere cappi e tolgo le coppie uguali(prendo 1,5 e non 5,1) perché grafo NON ORIENTATO
					int peso = exhibitionComuni(aop,aoa);
					//se faccio query ogni volta son lenta! chiedo direttamente al db non tutti i pesi ma solo quelli!=0
					
					if(peso!=0) {
//						DefaultWeightedEdge e = this.graph.addEdge(aop, aoa);
						//OCCHIO SE ARCO C'è GIà, IL METODO Dà NULL=>NullPointerException perché grafo non orientato
//						this.graph.setEdgeWeight(e, peso);
						
						System.out.format("(%d,%d) peso %d\n",aop.getId(),aoa.getId(),peso);
							
						Graphs.addEdge(this.graph, aop, aoa, peso);
					}
				}
			}
		}
		*/
	}


	private int exhibitionComuni(ArtObject aop, ArtObject aoa) {
		ArtsmiaDAO dao = new ArtsmiaDAO();//non costa creare un altro dao perchè non ha costruttore
		int comuni = dao.contaExhibitionComuni(aop,aoa);
		return comuni;
	}
	
}
