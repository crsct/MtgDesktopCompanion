 function $_GET(param) 
 {
	    	var vars = {};
	    	window.location.href.replace( location.hash, '' ).replace( 
	    		/[?&]+([^=&]+)=?([^&]*)?/gi, // regexp
	    		function( m, key, value ) { // callback
	    			vars[key] = value !== undefined ? value : '';
	    		}
	    	);

	    	if ( param ) {
	    		return vars[param] ? vars[param] : null;	
	    	}
	    	return vars;
}
 

function generateEditionHTML(data)
{
				var append="<div class='col-md-2'>";
           			append+="<p class='card-text'><i class='ss ss-3x ss-"+data.keyRuneCode.toLowerCase()+"'></i>"+data.set+"</p>";
                	append+="</div>";
                	
         return append;
}

function generateStockSealedHTML(data,currency, tosell, percentReduction)
{
		if(!data)
			return;
			
			var append="<div class='col-sm'>";
  					append+="<div class='card'>";
        			if(data.product.url)
        				append+="<img class='card-img-top' src='"+data.product.url+"' alt='"+data.product.edition.set+"'>";
        			else
        				append+="<img class='card-img-top' src='https://www.generationsforpeace.org/wp-content/uploads/2018/03/empty-300x240.jpg' alt='"+data.product.edition.set+"'>";
        				
    				
       				append+="<div class='card-body'>";
           			
           			append+="<h5 class='card-title'><a href='product.html?id="+data.id+"' title='View Product'>"+data.product.type +"</a></h5>";
           				
           			append+="<p class='card-text'>";
							append+="<i class='ss ss-1x ss-"+data.product.edition.id.toLowerCase()+"'></i>"+data.product.edition.set +"<br/>";
							append+= data.condition + " " + (data.foil?"<i class='fas fa-star fa-1x'/>":"") ;
							
					append+="</p>";
							
							
							
           			append+="<div class='row'>";
                	append+="<div class='col'>";
                	
					if(percentReduction>0)
                		append+="<p class='btn btn-danger btn-block'>"+(data.price-(data.price*percentReduction)).toFixed(2)+" " + currency +  "</p>";
                	else
                		append+="<p class='btn btn-danger btn-block'>"+data.price.toFixed(2)+" " + currency +  "</p>";
                		
                		
                	append+="</div>";
               		append+="<div class='col'>";
               		
               		               		
               		if(data.qte>=1)                                       		
                    	append+="<button name='addCartButton' qty='"+ data.qte +"' data='"+ data.id+"' type='sealed' class='btn btn-success btn-block'><i class='fas fa-cart-plus' ></i> Add to cart </button>";
                    else
                    	append+="<span class='btn btn-secondary btn-block'><i class='fa fa-shopping-cart'></i>Out of stock</span>";
               		
                     	
                    	
                	append+="</div></div></div></div></div>";
              
                	
                 
                	
         return append;

			
			
}


	   
function generateStockCardHTML(data,currency, tosell, percentReduction)
{

			if(!data)
				return;
			
			var append="<div class='col-sm'>";
  					append+="<div class='card'>";
        			
        			if(data.product.scryfallId!=null)
    					append+="<img class='card-img-top' src='https://api.scryfall.com/cards/"+data.product.scryfallId+"?format=image' alt='"+data.product.name +"'>";
    				else
    					append+="<img class='card-img-top' src='https://api.scryfall.com/cards/multiverse/"+data.product.editions[0].multiverseId+"?format=image' alt='Card image cap'>";
    				
       				append+="<div class='card-body'>";
           			
           			append+="<h5 class='card-title'><a href='product.html?id="+data.id+"' title='View Product'>"+data.product.name +"</a></h5>";
           				
           			append+="<p class='card-text'>";
							append+="<i class='ss ss-1x ss-"+data.product.editions[0].id.toLowerCase()+"'></i>"+data.product.editions[0].set +"<br/>";
							append+= data.condition + " " + (data.foil?"<i class='fas fa-star fa-1x'/>":"") ;
							
					append+="</p>";
							
							
							
           			append+="<div class='row'>";
                	append+="<div class='col'>";
                	
					if(percentReduction>0)
                		append+="<p class='btn btn-danger btn-block'>"+(data.price-(data.price*percentReduction)).toFixed(2)+" " + currency +  "</p>";
                	else
                		append+="<p class='btn btn-danger btn-block'>"+data.price.toFixed(2)+" " + currency +  "</p>";
                		
                		
                	append+="</div>";
               		append+="<div class='col'>";
               		
               			
               		if(tosell===true)
               		{
               			append+="<button name='addCartButton' data-dismiss='alert'  data='"+ data.id+"' class='btn btn-success btn-block' sell='true' ><i class='fa fa-shopping-cart'></i> Deal it</button>";
               		}
               		else
               		{
               		               		
               		if(data.qte>=1)                                       		
                    	append+="<button name='addCartButton' qty='"+ data.qte +"' data='"+ data.id+"' type='stock' class='btn btn-success btn-block'><i class='fas fa-cart-plus' ></i> Add to cart </button>";
                    else
                    	append+="<span class='btn btn-secondary btn-block'><i class='fa fa-shopping-cart'></i>Out of stock</span>";
                    	
               		
               		}
               		
                     	
                    	
                	append+="</div></div></div></div></div>";
              
                	
                 
                	
         return append;

}
