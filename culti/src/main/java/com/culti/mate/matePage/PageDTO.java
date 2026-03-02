package com.culti.mate.matePage;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PageDTO {
	
	private int startPage;  // 시작번호
	private int endPage;    // 끝번호
	private boolean prev;
	private boolean next;
	
	private int total;
	private Criteria criteria;
	
	public PageDTO(Criteria criteria, int total) {
		this.criteria = criteria;
	    this.total = total;
	    
	    this.endPage = (int)(Math.ceil(criteria.getPageNum()/
	    		   (double)criteria.getAmount())) * criteria.getAmount();
	    this.startPage = this.endPage - criteria.getAmount() + 1;
	    
	    int realEndPage = (int)(Math.ceil((double)total/criteria.getAmount()));
	    if(realEndPage < this.endPage) this.endPage = realEndPage;
	    
	    this.prev = this.startPage > 1;
	    this.next = this.endPage < realEndPage;
	    
	    System.out.printf("🤩🤩 %d, %d\n", startPage, endPage);
	}
	 

}