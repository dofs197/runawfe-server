/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.web.tag;

import java.util.List;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.CssClassStrategy;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ProcessRowBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created on 15.10.2004
 *
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listProcessesForm")
public class ListProcessesFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 585180395259884607L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        BatchPresentation batchPresentation = getBatchPresentation();
        ExecutionService executionService = Delegates.getExecutionService();

        int instanceCount = executionService.getProcessesCount(getUser(), batchPresentation);
        // we must call getProcesses before obtaining current page number
        // since it can be changed after getProcesses call
        List<WfProcess> processes = executionService.getProcesses(getUser(), batchPresentation);
        // batchPresentation must be recalculated since the current page
        // number might changed
        batchPresentation = getBatchPresentation();
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, instanceCount, getReturnAction());
        navigation.addPagingNavigationTable(tdFormElement);

        TDBuilder[] builders = BatchPresentationUtils.getBuilders(null, batchPresentation, null);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, new String[0], new String[0], getReturnAction(), pageContext);

        boolean isFilterable = false;
        int idx = 0;
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        for (FieldDescriptor field : fields) {
            if (field.displayName.startsWith(ClassPresentation.filterable_prefix) && batchPresentation.isFieldGroupped(idx)) {
                isFilterable = true;
                break;
            }
            idx++;
        }

        ReflectionRowBuilder rowBuilder = isFilterable ? new ProcessRowBuilder(processes, batchPresentation, pageContext,
                ShowGraphModeHelper.getManageProcessAction(), getReturnAction(), "id", builders) : new ReflectionRowBuilder(processes,
                batchPresentation, pageContext, ShowGraphModeHelper.getManageProcessAction(), getReturnAction(), "id", builders);
        rowBuilder.setCssClassStrategy(new ProcessCssClassStrategy());

        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder, isFilterable ? true : false));

        navigation.addPagingNavigationTable(tdFormElement);
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return false;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESSES.message(pageContext);
    }

    static class ProcessCssClassStrategy implements CssClassStrategy {

        @Override
        public String getClassName(Object item, User user) {
            WfProcess p = (WfProcess) item;
            if (p.getExecutionStatus() == ExecutionStatus.SUSPENDED || p.getExecutionStatus() == ExecutionStatus.FAILED) {
                return Resources.CLASS_SUSPENDED;
            }
            return "";
        }

        @Override
        public String getCssStyle(Object item) {
            return null;
        }

    }

}
