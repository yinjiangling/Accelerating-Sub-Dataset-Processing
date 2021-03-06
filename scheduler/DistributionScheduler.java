package scheduler;

import approximationmeta.src.*;
import java.util.*;

public class DistributionScheduler {

	public	HashMap<String, LinkedList<String>> defaultAssignment;
	public 	HashMap<String, LinkedList<String>> claspAssignment;
	public  HashMap<String, LinkedList<String>> assignment;	
	public void printAssign(NodesToFile ntf, int flag){
		HashMap<String, LinkedList<String>> ass = null;
		if(flag == 0) ass = claspAssignment;
		if(flag == 1) ass = defaultAssignment;
		if(ass == null)
			return ;
		for(String cpt : ntf.computers){
			String s = cpt;
			for(String e : ass.get(cpt))
				s += e;
			System.out.println(s);
		}
	}

	public void balanceAssign(LinkedList<String> slaves, MetaServer ms, String movieID){
		ms.sortDistribution(movieID);

		claspAssignment = new HashMap<String, LinkedList<String>>();
		HashMap<String, FileWorkload> hfw = new HashMap<String, FileWorkload>();
		HashSet<PartitionStatistic> remoteFiles = new HashSet<PartitionStatistic>();
			
		for(String s : slaves){	
			claspAssignment.put(s, new LinkedList<String>());
			hfw.put(s, new FileWorkload());
		}

		double count = slaves.size();
		for(int i=0; i < ms.globalMap.length; i++)
			count += ms.globalMap[i].quantity;

		double average = count/slaves.size();
		
		for(int i=0; i < ms.globalMap.length && ms.globalMap[i].quantity != 0;i++){
			String[] hosts = ms.globalMap[i].hosts;
			String h = hosts[0];
			for(int j=1; j < hosts.length; j++){
				if(hfw.get(h) == null)
					h = hosts[j];
				else if (hfw.get(hosts[j])!= null && hfw.get(hosts[j]).workload < hfw.get(h).workload)
					h = hosts[j];
			}
			if(hfw.get(h) != null && hfw.get(h).workload < average){
				hfw.get(h).workload += ms.globalMap[i].quantity; 
				claspAssignment.get(h).add(ms.globalMap[i].fileName);
			}
			else
			   remoteFiles.add(ms.globalMap[i]);	
		}
		for(PartitionStatistic file : remoteFiles) {
			for(String host : slaves)
				if(hfw.get(host).workload < average){
					claspAssignment.get(host).add(file.fileName);
					hfw.get(host).workload += file.quantity; 
				}
		}
		assignment = claspAssignment;
	}
	
	public void defaultAssign(MetaServer ms, LinkedList<String> slaves){
		defaultAssignment = new HashMap<String, LinkedList<String>>();	
		HashSet<String> setFile = new HashSet<String>();	
		
		for(String s : slaves)
			defaultAssignment.put(s, new LinkedList<String>());
		
		int totalfiles = ms.globalMap.length;
		int average = (int)totalfiles/slaves.size();
		if(average * slaves.size() < totalfiles)
			average +=1;

		for(int i =0; i < ms.globalMap.length;i++){
			String[] hosts = ms.globalMap[i].hosts;
			boolean ass = false;
			for(String host : hosts){					
				LinkedList<String> ll = defaultAssignment.get(host);
				if(ll != null && ll.size() < average){
					ll.add(ms.globalMap[i].fileName); ass = true; break;
				}		
			}
			if(ass == false) 
				setFile.add(ms.globalMap[i].fileName);		
		}
		
		for(String e: setFile){
			for(String host : slaves){
				LinkedList<String> ll = defaultAssignment.get(host);
				if(ll.size()<average) {
					ll.add(e);
				}
			}
		}
		assignment = defaultAssignment;
	}
}


