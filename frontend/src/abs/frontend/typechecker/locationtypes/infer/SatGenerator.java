package abs.frontend.typechecker.locationtypes.infer;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sat4j.maxsat.WeightedMaxSatDecorator;
import org.sat4j.maxsat.reader.WDimacsReader;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;

import abs.frontend.analyser.SemanticErrorList;
import abs.frontend.typechecker.locationtypes.LocationType;

public class SatGenerator {
    final Set<Constraint> constraints;
    final Set<LocationTypeVariable> vars;
    private boolean enableStats = false;
    private boolean enableDebug = false;
    
    final List<List<Integer>> output;
    
    final Environment e;
    
    
    
    
    public SatGenerator(Set<Constraint> constraints) {
        this.constraints = constraints;
        initializeConstraints();
        e = new Environment();
        output = new ArrayList<List<Integer>>();
        vars = new HashSet<LocationTypeVariable>();
    }
    
    private void initializeConstraints() {
        for (LocationType lt : LocationType.ALLVISTYPES) {
            LocationTypeVariable cltv = LocationTypeVariable.getFromLocationType(lt);
            constraints.add(Constraint.declConstraint(cltv));
            constraints.add(Constraint.constConstraint(cltv, lt, Constraint.MUST_HAVE));
        }
    }

    public Map<LocationTypeVariable, LocationType> generate(SemanticErrorList s) {
        Map<LocationTypeVariable, LocationType> res = generate();
        
        if (res == null) {
            s.add(new LocationInferenceError());
        }
        
        return res;
        
    }
        
    public Map<LocationTypeVariable, LocationType> generate() {
        long startNanos = System.nanoTime();

        Map<LocationTypeVariable, LocationType> tvl = new HashMap<LocationTypeVariable, LocationType>();
        for (Constraint c : constraints) {
            if (enableDebug) System.out.println(c);
            List<List<Integer>> gen = c.generateSat(e);
            output.addAll(gen);
            c.variables(vars);
        }
        
        long genNanos = System.nanoTime();
        if (enableStats) {
            System.out.println("Constraint generation time: " + (genNanos - startNanos) / 1000000);
        }
        
        StringBuffer weights = new StringBuffer();
        for (LocationTypeVariable tv : vars) {
            weights.append(Constraint.NICE_TO_HAVE);
            weights.append(" ");
            weights.append(e.get(tv, LocationType.NEAR));
            weights.append(" ");
            weights.append(e.get(tv, LocationType.FAR));
            weights.append(" ");
            if (tv.getNode() == null) {
                for (LocationType lt : tv.parametricFarTypes()) {
                    weights.append(e.get(tv, lt));
                    weights.append(" ");
                }
            }
            weights.append("0\n");
        }
        
        StringBuffer sb = new StringBuffer();
        int nbclauses = output.size() + vars.size();
        int nbvars = e.current;
        
        addInitLine(sb,nbclauses,nbvars);
        
        for (List<Integer> line : output) {
            for (Integer i : line) {
                sb.append(i);
                sb.append(" ");
            }
            sb.append("0\n");
        }
        
        sb.append(weights);
        
        
        if (enableStats) {
           System.out.println("Number of variables: " + nbvars);
           System.out.println("Number of clauses: " + nbclauses);
        }
        
        //System.out.println(sb);
        
        IPBSolver solver = org.sat4j.maxsat.SolverFactory.newDefault();//instance().defaultSolver();
        
        //System.exit(0);
        
        WeightedMaxSatDecorator wmsd = new WeightedMaxSatDecorator(solver);
        
        WDimacsReader reader = new WDimacsReader(wmsd);
        
        try {
            InputStream is = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
            IProblem problem = reader.parseInstance(is);
            long parseNanos = System.nanoTime();
            if (enableStats) {
                System.out.println("Parsing time: " + (parseNanos - genNanos) / 1000000);
            }
            OptToPBSATAdapter opt = new OptToPBSATAdapter(wmsd);
            opt.setVerbose(false);
            //opt.setTimeout(arg0)
            if (opt.isSatisfiable()) {
                int[] model = opt.model();
                long solveNanos = System.nanoTime();
                if (enableStats) {
                   System.out.println("Solving time: " + (solveNanos-parseNanos) / 1000000);
                   System.out.println("Total time: " + (solveNanos-startNanos) / 1000000);
                }
                //int[] model = problem.model();
                //model = problem.findModel();
                //System.out.println("Model generated");
                //problem.printInfos(new PrintWriter(System.out), "INFO: ");
                //System.out.println(Arrays.toString(model));
                for (int i : model) {
                    if (i > 0 && i <= nbvars) {
                        TypedVar tv = e.vars().get(i-1);
                        //System.out.println(tv.v + " : " + tv.t);
                        tvl.put(tv.v, tv.t);
                    }   
                }
                if (enableDebug) System.out.println("Solution: " + tvl);
            } else {
                return null;
            }
            return tvl;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ContradictionException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private void addInitLine(StringBuffer sb, int nbclauses, int nbvars) {
        sb.append("p wcnf ");
        sb.append(nbvars);
        sb.append(" ");
        sb.append(nbclauses);
        sb.append(" ");
        sb.append(Constraint.MUST_HAVE);
        sb.append("\n");
    }
    
}