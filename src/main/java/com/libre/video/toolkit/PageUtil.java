package com.libre.video.toolkit;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.common.collect.Lists;
import com.libre.core.toolkit.CollectionUtil;
import com.libre.core.toolkit.StringUtil;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Order;

import java.util.Collections;
import java.util.List;

/**
 * @author: Libre
 * @Date: 2022/4/21 12:33 AM
 */
@UtilityClass
public class PageUtil<T> {


	public static  <T> List<Sort.Order> getOrders(PageDTO<T> page) {
		List<OrderItem> orderItems = page.getOrders();
		if (CollectionUtil.isEmpty(orderItems)) {
			return Collections.emptyList();
		}

		List<Sort.Order> orders = Lists.newArrayList();
		for (OrderItem order : orderItems) {
			if (StringUtil.isNotBlank(order.getColumn())) {
				orders.add(new Sort.Order(getDirection(order.isAsc()), order.getColumn()));
			}
		}
		return orders;
	}

	private Sort.Direction getDirection(Boolean isAsc) {
		if (Boolean.TRUE.equals(isAsc)) {
			return Sort.Direction.ASC;
		} else if(Boolean.FALSE.equals(isAsc)){
			return Sort.Direction.DESC;
		} else {
			return Sort.Direction.ASC;
		}
	}
}
