package com.lvmama.vst.order.service;

import com.lvmama.vst.order.vo.OrdItemShowTicketInfoVO;

public interface OrdItemShowTicketInfoService {
    public void insert(OrdItemShowTicketInfoVO showTicketInfo);
    public OrdItemShowTicketInfoVO queryByOrdItemId(Long ordItemId);
}
