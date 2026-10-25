package com.quinn.hboot.family.controller;
import com.baomidou.mybatisplus.core.metadata.IPage; import com.quinn.hboot.common.R; import com.quinn.hboot.family.model.dto.FamilyRequests; import com.quinn.hboot.family.model.entity.*; import com.quinn.hboot.family.service.FamilyAssetService; import io.swagger.v3.oas.annotations.tags.Tag; import jakarta.servlet.http.HttpServletResponse; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import java.util.List;
@Tag(name="家庭数字资产") @RestController @RequestMapping("/api/v1/family-assets") @RequiredArgsConstructor
public class FamilyAssetController {
 private final FamilyAssetService service;
 @GetMapping("/documents") public R<IPage<FamilyDocument>> documents(@RequestParam(required=false)String keyword,@RequestParam(required=false)String category,@RequestParam(defaultValue="1")long page,@RequestParam(defaultValue="20")long size){return R.ok(service.documents(keyword,category,page,size));}
 @PostMapping("/documents") public R<FamilyDocument> uploadDocument(@RequestPart("file")MultipartFile file,@RequestParam String title,@RequestParam(defaultValue="OTHER")String category,@RequestParam(required=false)String tags,@RequestParam(required=false)String changeNote)throws Exception{return R.ok(service.uploadDocument(file,new FamilyRequests.DocumentSave(title,category,tags,changeNote)));}
 @PutMapping("/documents/{id}") public R<FamilyDocument> updateDocument(@PathVariable Long id,@RequestBody @Valid FamilyRequests.DocumentSave request){return R.ok(service.updateDocument(id,request));}
 @GetMapping("/documents/{id}/versions") public R<List<FamilyDocumentVersion>> versions(@PathVariable Long id){return R.ok(service.versions(id));}
 @DeleteMapping("/documents/{id}") public R<Void> deleteDocument(@PathVariable Long id){service.deleteDocument(id);return R.ok();}
 @GetMapping("/documents/{id}/content") public void documentContent(@PathVariable Long id,HttpServletResponse response)throws Exception{service.writeDocument(id,response,false);}
 @GetMapping("/documents/{id}/download") public void documentDownload(@PathVariable Long id,HttpServletResponse response)throws Exception{service.writeDocument(id,response,true);}
 @PostMapping("/duplicates/scan") public R<List<PhotoDuplicateCandidate>> scanDuplicates(){return R.ok(service.scanDuplicates());}
 @GetMapping("/duplicates") public R<List<PhotoDuplicateCandidate>> duplicates(){return R.ok(service.duplicates());}
 @PutMapping("/duplicates/{id}") public R<Void> markDuplicate(@PathVariable Long id,@RequestParam boolean marked){service.markDuplicate(id,marked);return R.ok();}
 @PostMapping("/shares") public R<FamilyShareLink> createShare(@RequestBody @Valid FamilyRequests.ShareSave request){return R.ok(service.createShare(request));}
 @GetMapping("/shares") public R<List<FamilyShareLink>> shares(){return R.ok(service.shares());}
 @DeleteMapping("/shares/{id}") public R<Void> revokeShare(@PathVariable Long id){service.revokeShare(id);return R.ok();}
 @GetMapping("/share/{token}") public R<FamilyShareLink> resolveShare(@PathVariable String token){return R.ok(service.resolveShare(token));}
 @GetMapping("/share/{token}/content") public void shareContent(@PathVariable String token,HttpServletResponse response)throws Exception{service.writeShareContent(token,response);}
 @PostMapping("/devices") public R<FamilyDevice> registerDevice(@RequestBody @Valid FamilyRequests.DeviceSave request){return R.ok(service.registerDevice(request));}
 @GetMapping("/devices") public R<List<FamilyDevice>> devices(){return R.ok(service.devices());}
 @GetMapping("/members") public R<List<com.quinn.hboot.photo.model.entity.PhotoSpaceMember>> members(){return R.ok(service.members());}
 @PostMapping("/members") public R<com.quinn.hboot.photo.model.entity.PhotoSpaceMember> saveMember(@RequestBody @Valid FamilyRequests.MemberSave request){return R.ok(service.saveMember(request));}
 @DeleteMapping("/members/{userId}") public R<Void> revokeMember(@PathVariable Long userId){service.revokeMember(userId);return R.ok();}
 @PostMapping("/devices/{id}/sync") public R<List<FamilySyncLog>> sync(@PathVariable Long id,@RequestBody FamilyRequests.SyncPush request){return R.ok(service.pushSync(id,request));}
 @GetMapping("/devices/{id}/sync-log") public R<List<FamilySyncLog>> syncLogs(@PathVariable Long id){return R.ok(service.syncLogs(id));}
}
