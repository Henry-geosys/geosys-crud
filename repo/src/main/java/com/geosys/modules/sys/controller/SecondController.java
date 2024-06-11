package com.geosys.modules.sys.controller;

import com.geosys.modules.sys.entity.SysDemoEntity;
import com.geosys.modules.sys.service.SysDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import com.geosys.common.utils.R;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/sys_demo2")
public class SecondController {


    private static final Logger logger = LoggerFactory.getLogger(SecondController.class);

    @Autowired
    private SysDemoService sysdemoService;



    @GetMapping("/get_demo/{id}")
    public R getSysDemoById(@PathVariable Long id) {
        try {
            SysDemoEntity demo = sysdemoService.getById(id);
            if (demo != null) {
                return R.ok().put("demo", demo);
            } else {
                return R.error("Demo not found");
            }
        } catch (Exception e) {
            return R.error("Error fetching demo");
        }
    }

    @GetMapping("/get_all_demos")
    public R getAllDemos() {
        try {
            List<SysDemoEntity> demos = sysdemoService.list();
            return R.ok().put("demos", demos);
        } catch (Exception e) {
            return R.error("Error fetching demos");
        }
    }

    @PostMapping("/create_demo")
    public R createDemo(@RequestBody SysDemoEntity demoEntity) {
        try {
            sysdemoService.save(demoEntity);
            return R.ok().put("message", "Demo created successfully").put("demoId", demoEntity.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("Error creating demo");
        }
    }

    @DeleteMapping("/delete_demo/{id}")
    public R deleteDemoById(@PathVariable Long id) {
        try {
            sysdemoService.removeById(id);
            return R.ok().put("message", "Demo deleted successfully").put("demoId", id);
        } catch (Exception e) {
            return R.error("Error deleting demo");
        }
    }

    @PutMapping("/update_demo/{id}")
    public R updateDemo(@PathVariable Long id, @RequestBody SysDemoEntity updatedSysDemo) {
        try {
            updatedSysDemo.setId(id);
            sysdemoService.updateById(updatedSysDemo);
            return R.ok().put("message", "Demo updated successfully").put("demoId", id);
        } catch (Exception e) {
            return R.error("Error updating demo");
        }
    }
}