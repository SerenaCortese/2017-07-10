package it.polito.tdp.artsmia.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import it.polito.tdp.artsmia.db.ArtsmiaDAO;

public class Model {
	
	private List<ArtObject> artObjects; //mi serve elenco degli oggetti
	private Graph<ArtObject,DefaultWeightedEdge> graph; //poi costruisco grafo con quelli
	
	private List<ArtObject> best;
	
	/**
	 * Popola la lista artObjects (leggendo dal DB) e crea il grafo
	 */
	public void creaGrafo() {
		
		//1) leggo lista oggetti da DB 
		//(ogni volta che chiamo il metodo butta via lista precedente e ne crea una nuova leggendo da db)
		ArtsmiaDAO dao = new ArtsmiaDAO();
		this.artObjects = dao.listObjects();
		System.out.format("Oggetti caricati: %d oggetti\n", this.artObjects.size());
		
		//2) creo oggetto grafo (ogni volta che chiamo metodo crea grafo nuovo)
		this.graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//3) aggiungo vertici ( = tutti gli oggetti)
//		for (ArtObject ao : this.artObjects) {
//			this.graph.addVertex(ao); 
//		}
//		
		//adesso ao fa parte di un set =>IMPORTANTE DEFINIRE HASHCODE e EQUALS in ArtObject
		Graphs.addAllVertices(this.graph, this.artObjects);
		
		//4) aggiungo archi con il loro peso
		addEdgesV2();
		System.out.format("Grafo creato: %d vertici, %d archi\n", graph.vertexSet().size(), graph.edgeSet().size());

	}
	
	/**
	 * Aggiungi gli archi al grafo
	 * 
	 * VERSIONE 1 - per nulla efficiente ** esegue una query per ogni coppia di
	 * vertici
	 */
	private void addEdgesV1() {
		for (ArtObject aop : this.artObjects) {
			for (ArtObject aoa : this.artObjects) {
				if (!aop.equals(aoa) && aop.getId() < aoa.getId()) { // escludo coppie (ao, ao) per escludere i loop
																//e le coppie uguali (1,5)(5,1) perché grafo NON ORIENTATO
					int peso = exhibitionComuni(aop, aoa);

					if (peso != 0) {
						// DefaultWeightedEdge e = this.graph.addEdge(aop, aoa) ;
						//OCCHIO! se arco c'è già, il metodo genera Null=>NullPointerException perché grafo non orientato
						// graph.setEdgeWeight(e, peso);
						System.out.format("(%d, %d) peso %d\n", aop.getId(), aoa.getId(), peso);


						Graphs.addEdge(this.graph, aop, aoa, peso);
					}
				}
			}
		}
	}

	/**
	 * Aggiungi gli archi al grafo
	 * 
	 * VERSIONE 2 - più efficiente ** Utilizza il metodo listArtObjectAndCount per
	 * fare una sola query per vertice, ottenendo in un solo colpo tutti gli archi
	 * adiacenti a tale vertice (ed il relativo peso)
	 */
	private void addEdgesV2() {
		ArtsmiaDAO dao = new ArtsmiaDAO();
		for (ArtObject ao : this.artObjects) {
			List<ArtObjectAndCount> connessi = dao.listArtObjectAndCount(ao);

			for (ArtObjectAndCount c : connessi) {
				ArtObject dest = new ArtObject(c.getArtObjectId(), null, null, null, 0, null, null, null, null, null, 0,
						null, null, null, null, null); // l'oggetto "dest" è un ArtObject inizializzato in modo
														// 'lazy', cioè solo con i campi utili per il calcolo di
														// hashCode/equals. In questo modo può "impersonare" un vertice
														// del grafo.
				////oggetto debole, meglio fare una IdMappa all'inizio che inizializzo quando prendo artObjects dal dao
				// System.out.format("(%d, %d) peso %d\n", ao.getId(), dest.getId(),
				// c.getCount()) ;
				Graphs.addEdge(this.graph, ao, dest, c.getCount());
			}
		}

	}

	/**
	 * Aggiungi gli archi al grafo
	 * 
	 * VERSIONE 3 - la più efficiente di tutte ** esegue una query unica (complessa,
	 * ma una sola) con la quale ottiene in un sol colpo tutti gli archi del grafo
	 * 
	 */
	private void addEdgesV3() {
		// TODO: basata sulla query:
		// SELECT eo1.object_id, count(eo2.exhibition_id), eo2.object_id
		// FROM exhibition_objects eo1, exhibition_objects eo2
		// WHERE eo1.exhibition_id=eo2.exhibition_id
		// AND eo2.object_id>eo1.object_id
		// GROUP BY eo1.object_id, eo2.object_id

	}

	private int exhibitionComuni(ArtObject aop, ArtObject aoa) {
		ArtsmiaDAO dao = new ArtsmiaDAO();//non costa creare un altro dao perchè non ha costruttore
		
		int comuni = dao.contaExhibitionComuni(aop,aoa);
		
		return comuni;
	}
	
	public int getGraphNumEdges() {
		return this.graph.edgeSet().size() ;
	}
	
	public int getGraphNumVertices() {
		return this.graph.vertexSet().size() ;
	}


	public boolean isObjIdValid(int idObj) {//se è una funzione che chiamo spesso mi conviene fare una idMap
		//non è detto che questa funzione venga chiamata dopo aver creato il grafo
		//potrei gestire questa eccezione da controller
		
		if(this.artObjects == null)
			return false;
		
		for(ArtObject ao : this.artObjects) {
			if(ao.getId() == idObj)
				return true;
		}
		return false;
	}


	public int calcolaDimensioneCC(int idObj) { 
		
		//trova il vertice di partenza 
		ArtObject start = this.trovaVertice(idObj);
		
		//visita il grafo
		Set<ArtObject> visitati = new HashSet<ArtObject>();//set perché non mi interessa ordine in cui son trovati ma solo quali
		DepthFirstIterator<ArtObject,DefaultWeightedEdge> dfv = new DepthFirstIterator<>(this.graph, start);//gli dico vertice da cui partire
		
		while(dfv.hasNext()) { //ogni volta che va avanti di un passo mi salvo il vertice successivo
			visitati.add(dfv.next());
		}
		
		//conta gli elementi
		return visitati.size();
	}

	public List<ArtObject> getArtObjects() {
		return artObjects;
	}
	private ArtObject trovaVertice(int idObj) {
		//trova il vertice di parteza
		ArtObject start = null;
		for(ArtObject ao : this.artObjects) {
			if(ao.getId() == idObj) {
				start = ao;
				break;
			}
		}
		if(start == null)//non dovrebbe succedere perché nel controller ho controllato già che esistessse quell'id
			throw new IllegalArgumentException("Vertice "+ idObj+ " non esistente.");
		return start;
	}
	
	//SOLUZIONE PUNTO 2
	
	public List<ArtObject> camminoMassimo(int startId, int LUN){
		//trovare vertice di partenza
		ArtObject start = trovaVertice(startId);
		
		List<ArtObject> parziale = new ArrayList<>();
		parziale.add(start);
		
		this.best = new ArrayList<>();
		best.add(start);//tanto il peso di questa funzione =0 e vien subito rimpiazzato
		
		cerca(parziale, 1, LUN);
		
		return best;
	}
	
	public void cerca(List<ArtObject> parziale, int livello, int LUN) {
		//condizione terminale
		if(livello == LUN) {
			if(peso(parziale) > peso(best)) {
				best= new ArrayList(parziale);
				System.out.println(parziale);
			}
			return;
		}
		
		//trova vertici adiacenti all'ultimo della sequenza
		ArtObject ultimo = parziale.get(parziale.size()-1);
		
		List<ArtObject> adiacenti = Graphs.neighborListOf(this.graph, ultimo);
		
		for(ArtObject prova : adiacenti) {
			if(!parziale.contains(prova) && prova.getClassification().equals(parziale.get(0).getClassification())) {
				parziale.add(prova);
				cerca(parziale,livello+1,LUN);
				parziale.remove(parziale.size()-1);
			}
		}
	}

	private int peso(List<ArtObject> parziale) {
		int peso = 0;
		for(int i = 0; i<parziale.size()-1;i++) {
			DefaultWeightedEdge e = this.graph.getEdge(parziale.get(i),parziale.get(i+1));
			int pesoArco = (int) this.graph.getEdgeWeight(e);
			peso+= pesoArco;
		}
		
		return peso;
	}
	
	
}
