(:
	Module containing functions to deal with probabilistic XML. 
:)

(: use Maurice van Keulen's home page as module namespace for the time being :)
module namespace wsd = 'http://www.utwente.nl/~keulen/pxml/';

(:
	Combines a list of conditions into a single string containing unique 
	conditions (variables with different values are considered different).
:)
declare function wsd:combine($conditions as xs:sequence) as xs:string {
	
};

(:
	Checks whether a string containing descriptions is consistent (that is: 
	whether it contains the same variable names with different values).
:)
declare function wsd:consistent($conditions as xs:string) as xs:bool {
	
};

(:
	Checks if two strings contain descriptors that would make the sequences 
	mutually exclusive (that is: whether one contains variables that require a 
	different value in the other).
:)
declare function wsd:mutually-exclusive($a as xs:string, $b as xs:string) as xs:bool {
	
};

(:
	Returns the probability of a set of variables having particular values.
:)
declare function wsd:probability($doc as xs:string, $conditions as xs:string) as xs:double {
	
};
