package com.dalstonsemantics.confluence.semantics.cloud;

import org.eclipse.rdf4j.query.algebra.Add;
import org.eclipse.rdf4j.query.algebra.AggregateFunctionCall;
import org.eclipse.rdf4j.query.algebra.And;
import org.eclipse.rdf4j.query.algebra.ArbitraryLengthPath;
import org.eclipse.rdf4j.query.algebra.Avg;
import org.eclipse.rdf4j.query.algebra.BNodeGenerator;
import org.eclipse.rdf4j.query.algebra.BindingSetAssignment;
import org.eclipse.rdf4j.query.algebra.Bound;
import org.eclipse.rdf4j.query.algebra.Clear;
import org.eclipse.rdf4j.query.algebra.Coalesce;
import org.eclipse.rdf4j.query.algebra.Compare;
import org.eclipse.rdf4j.query.algebra.CompareAll;
import org.eclipse.rdf4j.query.algebra.CompareAny;
import org.eclipse.rdf4j.query.algebra.Copy;
import org.eclipse.rdf4j.query.algebra.Count;
import org.eclipse.rdf4j.query.algebra.Create;
import org.eclipse.rdf4j.query.algebra.Datatype;
import org.eclipse.rdf4j.query.algebra.DeleteData;
import org.eclipse.rdf4j.query.algebra.DescribeOperator;
import org.eclipse.rdf4j.query.algebra.Difference;
import org.eclipse.rdf4j.query.algebra.Distinct;
import org.eclipse.rdf4j.query.algebra.EmptySet;
import org.eclipse.rdf4j.query.algebra.Exists;
import org.eclipse.rdf4j.query.algebra.Extension;
import org.eclipse.rdf4j.query.algebra.ExtensionElem;
import org.eclipse.rdf4j.query.algebra.Filter;
import org.eclipse.rdf4j.query.algebra.FunctionCall;
import org.eclipse.rdf4j.query.algebra.Group;
import org.eclipse.rdf4j.query.algebra.GroupConcat;
import org.eclipse.rdf4j.query.algebra.GroupElem;
import org.eclipse.rdf4j.query.algebra.IRIFunction;
import org.eclipse.rdf4j.query.algebra.If;
import org.eclipse.rdf4j.query.algebra.In;
import org.eclipse.rdf4j.query.algebra.InsertData;
import org.eclipse.rdf4j.query.algebra.Intersection;
import org.eclipse.rdf4j.query.algebra.IsBNode;
import org.eclipse.rdf4j.query.algebra.IsLiteral;
import org.eclipse.rdf4j.query.algebra.IsNumeric;
import org.eclipse.rdf4j.query.algebra.IsResource;
import org.eclipse.rdf4j.query.algebra.IsURI;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.Label;
import org.eclipse.rdf4j.query.algebra.Lang;
import org.eclipse.rdf4j.query.algebra.LangMatches;
import org.eclipse.rdf4j.query.algebra.LeftJoin;
import org.eclipse.rdf4j.query.algebra.Like;
import org.eclipse.rdf4j.query.algebra.ListMemberOperator;
import org.eclipse.rdf4j.query.algebra.Load;
import org.eclipse.rdf4j.query.algebra.LocalName;
import org.eclipse.rdf4j.query.algebra.MathExpr;
import org.eclipse.rdf4j.query.algebra.Max;
import org.eclipse.rdf4j.query.algebra.Min;
import org.eclipse.rdf4j.query.algebra.Modify;
import org.eclipse.rdf4j.query.algebra.Move;
import org.eclipse.rdf4j.query.algebra.MultiProjection;
import org.eclipse.rdf4j.query.algebra.Namespace;
import org.eclipse.rdf4j.query.algebra.Not;
import org.eclipse.rdf4j.query.algebra.Or;
import org.eclipse.rdf4j.query.algebra.Order;
import org.eclipse.rdf4j.query.algebra.OrderElem;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.ProjectionElem;
import org.eclipse.rdf4j.query.algebra.ProjectionElemList;
import org.eclipse.rdf4j.query.algebra.QueryModelNode;
import org.eclipse.rdf4j.query.algebra.QueryModelVisitor;
import org.eclipse.rdf4j.query.algebra.QueryRoot;
import org.eclipse.rdf4j.query.algebra.Reduced;
import org.eclipse.rdf4j.query.algebra.Regex;
import org.eclipse.rdf4j.query.algebra.SameTerm;
import org.eclipse.rdf4j.query.algebra.Sample;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.SingletonSet;
import org.eclipse.rdf4j.query.algebra.Slice;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.Str;
import org.eclipse.rdf4j.query.algebra.Sum;
import org.eclipse.rdf4j.query.algebra.Union;
import org.eclipse.rdf4j.query.algebra.ValueConstant;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.ZeroLengthPath;

import lombok.extern.slf4j.Slf4j;

/**
 * Use custom QueryModelVisitor to limit the set of statements that users can use with SPARQL. 
 * Want to avoid our servive used to trash other SPARQL endpoints.
 */
@Slf4j
public class SparqlRecursiveQueryModelVisitor implements QueryModelVisitor<SparqlForbiddenException> {

    @Override
    public void meet(QueryRoot node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Add node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(And node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(ArbitraryLengthPath node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Avg node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(BindingSetAssignment node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(BNodeGenerator node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Bound node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Clear node) throws SparqlForbiddenException {        
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(Coalesce node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Compare node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(CompareAll node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(CompareAny node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(DescribeOperator node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Copy node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(Count node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Create node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(Datatype node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(DeleteData node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(Difference node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Distinct node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(EmptySet node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Exists node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Extension node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(ExtensionElem node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Filter node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(FunctionCall node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(Group node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(GroupConcat node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(GroupElem node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(If node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(In node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(InsertData node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(Intersection node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(IRIFunction node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(IsBNode node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(IsLiteral node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(IsNumeric node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(IsResource node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(IsURI node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Join node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Label node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Lang node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(LangMatches node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(LeftJoin node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Like node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Load node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(LocalName node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(MathExpr node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Max node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Min node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Modify node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Move node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }

    @Override
    public void meet(MultiProjection node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Namespace node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Not node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Or node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Order node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(OrderElem node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Projection node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(ProjectionElem node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(ProjectionElemList node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Reduced node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Regex node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(SameTerm node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Sample node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Service node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(SingletonSet node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Slice node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(StatementPattern node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Str node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Sum node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Union node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(ValueConstant node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(ListMemberOperator node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(Var node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(ZeroLengthPath node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }

    @Override
    public void meet(AggregateFunctionCall node) throws SparqlForbiddenException {
        throw new SparqlForbiddenException();
    }
    
    @Override
    public void meetOther(QueryModelNode node) throws SparqlForbiddenException {
        node.visitChildren(this);
    }
}
